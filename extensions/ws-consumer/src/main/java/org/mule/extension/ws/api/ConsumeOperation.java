/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static org.mule.extension.ws.api.WSConsumer.SOAP_HEADERS_PROPERTY_PREFIX;
import static org.mule.runtime.core.MessageExchangePattern.REQUEST_RESPONSE;
import static org.mule.runtime.core.api.config.MuleProperties.OBJECT_CONNECTOR_MESSAGE_PROCESSOR_LOCATOR;
import static org.mule.runtime.core.util.Preconditions.checkState;
import static org.mule.runtime.extension.api.runtime.operation.OperationResultBuilderFactory.getDefaultFactory;
import static org.mule.runtime.module.http.api.HttpConstants.Methods.POST;
import static org.mule.runtime.module.http.api.HttpConstants.ResponseProperties.HTTP_STATUS_PROPERTY;
import static org.mule.runtime.module.http.api.client.HttpRequestOptionsBuilder.newOptions;
import org.mule.extension.ws.api.security.SecurityStrategy;
import org.mule.extension.ws.api.security.WSSecurity;
import org.mule.extension.ws.internal.InputSoapHeadersInterceptor;
import org.mule.extension.ws.internal.MP;
import org.mule.extension.ws.internal.NamespaceRestorerStaxInterceptor;
import org.mule.extension.ws.internal.NamespaceSaverStaxInterceptor;
import org.mule.extension.ws.internal.OutputSoapHeadersInterceptor;
import org.mule.extension.ws.internal.ProxyClientMessageProcessorBuilder;
import org.mule.extension.ws.internal.RequestBodyGenerator;
import org.mule.extension.ws.internal.SoapActionInterceptor;
import org.mule.extension.ws.internal.SoapVersion;
import org.mule.extension.ws.internal.exception.WSConsumerException;
import org.mule.runtime.core.api.MessagingException;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.MuleEvent;
import org.mule.runtime.core.api.MuleException;
import org.mule.runtime.core.api.MuleMessage;
import org.mule.runtime.core.api.connector.ConnectorOperationLocator;
import org.mule.runtime.core.api.processor.MessageProcessor;
import org.mule.runtime.core.api.processor.MessageProcessorChainBuilder;
import org.mule.runtime.core.config.i18n.CoreMessages;
import org.mule.runtime.core.processor.AbstractRequestResponseMessageProcessor;
import org.mule.runtime.core.processor.chain.DefaultMessageProcessorChainBuilder;
import org.mule.runtime.core.util.StringUtils;
import org.mule.runtime.extension.api.annotation.metadata.MetadataKeyId;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.UseConfig;
import org.mule.runtime.extension.api.runtime.operation.OperationResult;
import org.mule.runtime.module.cxf.CxfConstants;
import org.mule.runtime.module.http.api.client.HttpRequestOptions;
import org.mule.runtime.module.http.api.client.HttpRequestOptionsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Operation;

import org.apache.cxf.attachment.AttachmentImpl;
import org.apache.cxf.binding.soap.interceptor.CheckFaultInterceptor;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;

public class ConsumeOperation
{

    @Inject
    private MuleContext muleContext;

    public OperationResult consume(@UseConfig WSConsumer config,
                                   @Connection WSConsumerConnection connection,
                                   WSRequest request,
                                   MuleEvent event,
                                   @MetadataKeyId String operation) throws MuleException
    {


        WSResponseAttributesBuilder attributesBuilder = new WSResponseAttributesBuilder();
        Binding binding = connection.getBinding();
        BindingOperation bindingOperation = binding.getBindingOperation(operation, null, null);
        if (bindingOperation == null)
        {
            throw new WSConsumerException("Operation %s not found in WSDL", operation);
        }

        String soapAction = getSoapAction(bindingOperation);
        String requestBody = new RequestBodyGenerator(connection.getWsdlDefinition()).generateRequestBody(bindingOperation);

        copyRequestAttachments(request);


        MessageProcessorChainBuilder chainBuilder = new DefaultMessageProcessorChainBuilder();

        MuleEvent process = chainBuilder
                .chain(createCopyAttachmentsMessageProcessor(requestBody))
                .chain(createPropertyRemoverMessageProcessor(CxfConstants.OPERATION))
                .chain(createCxfOutboundMessageProcessor(connection.getSecurity(), config.isMtomEnabled(), connection.getSoapVersion(), soapAction, attributesBuilder))
                .chain(createSoapHeadersPropertiesRemoverMessageProcessor())
                .chain(createHttpRequester(connection))
                .build()
                .process(event);

        //String status = process.getMessage().getInboundProperty(HTTP_STATUS_PROPERTY).toString();
        //String reason = process.getMessage().getInboundProperty(HTTP_REASON_PROPERTY).toString();
        //
        //attributesBuilder.setStatusCode(Integer.parseInt(status)).setReasonPhrase(reason);

        return getDefaultFactory().create().output(process.getMessage().getPayload())
                .attributes(attributesBuilder.build())
                .build();

    }

    /**
     * Creates an http outbound endpoint for the service address.
     */
    public MessageProcessor createHttpRequester(WSConsumerConnection connection) throws MuleException
    {
        checkState(StringUtils.isNotEmpty(connection.getAddress()), "No serviceAddress provided in WS consumer config");

        return new MessageProcessor()
        {
            private HttpRequestOptions requestOptions;

            @Override
            public MuleEvent process(MuleEvent event) throws MuleException
            {
                ConnectorOperationLocator connectorOperationLocator = muleContext.getRegistry().get(OBJECT_CONNECTOR_MESSAGE_PROCESSOR_LOCATOR);
                MessageProcessor messageProcessor = connectorOperationLocator.locateConnectorOperation(connection.getAddress(), getRequestOptions(), REQUEST_RESPONSE);
                return messageProcessor.process(event);
            }

            private HttpRequestOptions getRequestOptions()
            {
                if (requestOptions == null)
                {
                    final HttpRequestOptionsBuilder builder = newOptions().method(POST.name()).disableStatusCodeValidation().disableParseResponse();
                    if (connection.getHttpRequesterConfig() != null)
                    {
                        builder.requestConfig(connection.getHttpRequesterConfig());
                    }
                    requestOptions = builder.build();
                }
                return requestOptions;
            }
        };
    }


    private Set<Attachment> copyRequestAttachments(WSRequest request)
    {
        /* If the requestBody variable is set, it will be used as the payload to send instead
         * of the payload of the message. This will happen when an operation required no input parameters. */
        if (!request.getAttachments().isEmpty())
        {

            return request.getAttachments()
                    .entrySet()
                    .stream()
                    .map(e -> new AttachmentImpl(e.getKey(), e.getValue()))
                    .collect(toSet());

            //event.setFlowVariable(CxfConstants.ATTACHMENTS, attachments);

            //event.setMessage(MuleMessage.builder(event.getMessage()).outboundAttachments(emptyMap()).build());
        }

        return emptySet();
    }

    //private AbstractRequestResponseMessageProcessor createCopyAttachmentsMessageProcessor(String requestBody)
    //{
    //    return new AbstractRequestResponseMessageProcessor()
    //    {
    //
    //        @Override
    //        protected MuleEvent processNext(MuleEvent event) throws MuleException
    //        {
    //            try
    //            {
    //                return super.processNext(event);
    //            }
    //            catch (DispatchException e)
    //            {
    //                /* When a Soap Fault is returned in the response, CXF raises a SoapFault exception.
    //                 * We need to wrap the information of this exception into a new exception of the WS consumer module */
    //
    //                if (e.getCause() instanceof SoapFault)
    //                {
    //                    SoapFault soapFault = (SoapFault) e.getCause();
    //
    //                    event.setMessage(MuleMessage.builder(event.getMessage()).payload(soapFault.getDetail() !=
    //                                                                                     null ? soapFault.getDetail()
    //                                                                                          : NullPayload
    //                                                                                             .getInstance())
    //                                             .build());
    //
    //                    throw new SoapFaultException(event, soapFault.getFaultCode(), soapFault.getSubCode(),
    //                                                 soapFault.getMessage(), soapFault.getDetail(), this);
    //                }
    //                else
    //                {
    //                    throw e;
    //                }
    //            }
    //        }
    //
    //        @Override
    //        protected MuleEvent processResponse(MuleEvent response, final MuleEvent request) throws MuleException
    //        {
    //            copyAttachmentsResponse(response);
    //            return super.processResponse(response, request);
    //        }
    //    };
    //}

    private MessageProcessor createPropertyRemoverMessageProcessor(final String propertyName)
    {
        return new AbstractRequestResponseMessageProcessor()
        {
            private Object propertyValue;

            @Override
            protected MuleEvent processRequest(MuleEvent event) throws MuleException
            {
                propertyValue = event.getFlowVariable(propertyName);
                event.removeFlowVariable(propertyName);
                return super.processRequest(event);
            }

            @Override
            protected MuleEvent processResponse(MuleEvent response, final MuleEvent request) throws MuleException
            {
                if (propertyValue != null)
                {
                    response.setFlowVariable(propertyName, propertyValue);
                }
                return super.processResponse(response, request);
            }
        };
    }

    private MessageProcessor createSoapHeadersPropertiesRemoverMessageProcessor()
    {
        return new AbstractRequestResponseMessageProcessor()
        {

            @Override
            protected MuleEvent processRequest(MuleEvent event) throws MuleException
            {
                // Remove outbound properties that are mapped to SOAP headers, so that the
                // underlying transport does not include them as headers.
                List<String> outboundProperties = new ArrayList<>(event.getMessage().getOutboundPropertyNames());

                MuleMessage.Builder builder = MuleMessage.builder(event.getMessage());
                for (String outboundProperty : outboundProperties)
                {
                    if (outboundProperty.startsWith(SOAP_HEADERS_PROPERTY_PREFIX))
                    {
                        builder.removeOutboundProperty(outboundProperty);
                    }
                }
                event.setMessage(builder.build());

                return super.processRequest(event);
            }

            @Override
            protected MuleEvent processResponse(MuleEvent response, final MuleEvent request) throws MuleException
            {
                // Ensure that the http.status code inbound property (if present) is a String.
                Object statusCode = response.getMessage().getInboundProperty(HTTP_STATUS_PROPERTY, null);
                if (statusCode != null && !(statusCode instanceof String))
                {
                    response.setMessage(MuleMessage.builder(response.getMessage()).addInboundProperty
                            (HTTP_STATUS_PROPERTY, statusCode.toString()).build());
                }
                return super.processResponse(response, request);
            }
        };
    }







    /**
     * Creates the CXF message processor that will be used to create the SOAP envelope.
     */
    private MP createCxfOutboundMessageProcessor(WSSecurity security,
                                                 boolean mtomEnabled,
                                                 SoapVersion version,
                                                 String soapAction,
                                                 WSResponseAttributesBuilder attributesBuilder) throws MuleException
    {
        ProxyClientMessageProcessorBuilder cxfBuilder = new ProxyClientMessageProcessorBuilder();
        Map<String, Object> outConfigProperties = new HashMap<>();
        Map<String, Object> inConfigProperties = new HashMap<>();

        cxfBuilder.setMtomEnabled(mtomEnabled);
        cxfBuilder.setSoapVersion(version.getVersion());

        if (security != null && security.hasStrategies())
        {
            for (SecurityStrategy strategy : security.getStrategies())
            {
                strategy.apply(outConfigProperties, inConfigProperties);
            }

            if (cxfBuilder.getOutInterceptors() == null)
            {
                cxfBuilder.setOutInterceptors(new ArrayList<>());
            }

            if (cxfBuilder.getInInterceptors() == null)
            {
                cxfBuilder.setInInterceptors(new ArrayList<>());
            }

            if (!outConfigProperties.isEmpty())
            {
                cxfBuilder.getOutInterceptors().add(new WSS4JOutInterceptor(outConfigProperties));
            }
            if (!inConfigProperties.isEmpty())
            {
                cxfBuilder.getInInterceptors().add(new WSS4JInInterceptor(inConfigProperties));
            }
        }


        MP cxfOutboundMessageProcessor = new MP();

        // We need this interceptor so that an exception is thrown when the response contains a SOAPFault.
        cxfOutboundMessageProcessor.getClient().getInInterceptors().add(new CheckFaultInterceptor());

        // CXF Interceptors that will ensure the SOAP body payload carries every namespace declaration from the
        // parent elements
        cxfOutboundMessageProcessor.getClient().getInInterceptors().add(new NamespaceSaverStaxInterceptor());
        cxfOutboundMessageProcessor.getClient().getInInterceptors().add(new NamespaceRestorerStaxInterceptor());

        if (soapAction != null)
        {
            cxfOutboundMessageProcessor.getClient().getOutInterceptors().add(new SoapActionInterceptor(soapAction));
        }

        cxfOutboundMessageProcessor.getClient().getOutInterceptors().add(new InputSoapHeadersInterceptor(muleContext));
        cxfOutboundMessageProcessor.getClient().getInInterceptors().add(new OutputSoapHeadersInterceptor(muleContext, attributesBuilder));


        return cxfOutboundMessageProcessor;
    }

    /**
     * Returns the SOAP action related to an operation, or null if not specified.
     */
    private String getSoapAction(BindingOperation bindingOperation)
    {
        List extensions = bindingOperation.getExtensibilityElements();
        for (Object extension : extensions)
        {
            if (extension instanceof SOAPOperation)
            {
                return ((SOAPOperation) extension).getSoapActionURI();
            }
            if (extension instanceof SOAP12Operation)
            {
                return ((SOAP12Operation) extension).getSoapActionURI();
            }
        }
        return null;
    }

    /**
     * Takes the set of CXF attachments from the CxfConstants.ATTACHMENTS invocation properties and sets
     * them as inbound attachments in the Mule Message.
     */
    private Set<Attachment> copyResponseAttachments() throws MessagingException
    {
        if (event.getFlowVariable(CxfConstants.ATTACHMENTS) != null)
        {
            Collection<Attachment> attachments = event.getFlowVariable(CxfConstants.ATTACHMENTS);
            MuleMessage.Builder builder = MuleMessage.builder(message);
            for (Attachment attachment : attachments)
            {
                try
                {
                    builder.addInboundAttachment(attachment.getId(), attachment.getDataHandler());
                }
                catch (Exception e)
                {
                    throw new MessagingException(CoreMessages.createStaticMessage("Could not set inbound attachment %s", attachment.getId()), event, e);
                }
            }
            event.setMessage(builder.build());
        }

    }

}
