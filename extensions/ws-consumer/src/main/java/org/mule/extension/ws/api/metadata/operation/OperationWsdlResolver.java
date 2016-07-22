/**
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.ws.api.metadata.operation;

import org.mule.extension.ws.api.metadata.utils.WsdlSchemaUtils;
import org.mule.extension.ws.api.metadata.utils.WsdlUtils;

import com.google.common.base.Optional;

import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.xml.transform.TransformerException;

public class OperationWsdlResolver
{
    private OperationIOResolver operationIOResolver;

    private Definition definition;
    private List<String> schemas;
    private Message message;
    private Optional<Part> messagePart;
    private Operation operation;
    List<SOAPHeader> operationHeaders;

    public OperationWsdlResolver(OperationIOResolver operationIOResolver, final Definition wsdlDefinition, final Binding binding, final String operationName) throws TransformerException
    {
        initialize(operationIOResolver, wsdlDefinition, binding, operationName);
    }

    private void initialize(OperationIOResolver operationIOResolver, Definition wsdlDefinition, Binding binding, String operationName) throws TransformerException
    {
        this.operationIOResolver = operationIOResolver;

        this.definition = wsdlDefinition;
        this.schemas = WsdlSchemaUtils.getSchemas(definition);

        PortType portType = binding.getPortType();
        this.operation =  WsdlUtils.getOperation(portType, operationName);

        this.message = operationIOResolver.getMessage(operation);
        WsdlUtils.validateNotNull(message, "There was an error while trying to resolve the message for the ["+operationName+"] operation.");

        BindingOperation bindingOperation = binding.getBindingOperation(operationName, null, null);
        operationHeaders = operationIOResolver.getHeaders(bindingOperation);

        messagePart = resolveMessagePart(bindingOperation);
    }

    private Optional<Part> resolveMessagePart(BindingOperation bindingOperation) {
        Map<?, ?> parts = message.getParts();
        if (!parts.isEmpty()) {
            if (parts.size() == 1)
            {
                //hack to behave the same way as before when the message has just one part
                Object firstValueKey = parts.keySet().toArray()[0];
                return Optional.of((Part) parts.get(firstValueKey));
            }else{
                Optional<String> bodyPartNameOptional = operationIOResolver.getBodyPartName(bindingOperation);
                if (bodyPartNameOptional.isPresent()){
                    return Optional.of((Part) parts.get(bodyPartNameOptional.get()));
                }else{
                    return Optional.absent();
                }
            }
        }
        return Optional.absent();
    }

    //getters
    public Definition getDefinition()
    {
        return definition;
    }

    public List<String> getSchemas()
    {
        return schemas;
    }

    public Optional<Part> getMessagePart()
    {
        return messagePart;
    }

    public List<SOAPHeader> getOperationHeaders()
    {
        return operationHeaders;
    }
}
