/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api.metadata;

import org.mule.common.metadata.datatype.DataType;
import org.mule.extension.ws.api.WSConsumerConnection;
import org.mule.extension.ws.api.metadata.operation.OperationWsdlResolver;
import org.mule.extension.ws.api.metadata.operation.OutputOperationResolver;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.metadata.resolving.MetadataAttributesResolver;
import org.mule.runtime.api.metadata.resolving.MetadataContentResolver;
import org.mule.runtime.api.metadata.resolving.MetadataOutputResolver;

import com.google.common.base.Optional;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.Part;
import javax.xml.transform.TransformerException;

public class WSOperationMetadataResolver implements MetadataOutputResolver<String>,
                                                    MetadataAttributesResolver<String>,
                                                    MetadataContentResolver<String>
{

    @Override
    public MetadataType getOutputMetadata(MetadataContext context, String operation) throws MetadataResolvingException, ConnectionException
    {
        OutputOperationResolver outputOperationResolver = new OutputOperationResolver();
        java.util.Optional<WSConsumerConnection> connection = context.getConnection();
        try
        {
            WSConsumerConnection wsConsumerConnection = connection.get();
            OperationWsdlResolver resolver= new OperationWsdlResolver(outputOperationResolver,
                                                                      wsConsumerConnection.getWsdlDefinition(),
                                                                      wsConsumerConnection.getBinding(),
                                                                      operation);
            URL url = getFileUrl(connection.get().getWsdlLocation());
            MetadataType metaData =  createMetaData(resolver.getSchemas(), resolver.getMessagePart(), url );
            //addProperties(metaData, resolver, outputOperationResolver.getScope());
            return metaData;
        }
        catch (TransformerException e)
        {
            //TODO how to propagate this to the UI? maybe we need a typed exception here
            return null;
        }    }

    @Override
    public MetadataType getContentMetadata(MetadataContext context, String operation) throws MetadataResolvingException, ConnectionException
    {
        //getMetaData(new InputOperationResolver());
        return null;
    }

    @Override
    public MetadataType getAttributesMetadata(MetadataContext context, String operation) throws MetadataResolvingException, ConnectionException
    {
        return null;
    }

    private URL getFileUrl(String fileURI) {
        try {
            return com.ibm.wsdl.util.StringUtils.getURL(null, fileURI);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private MetadataType createMetaData(List<String> schemas, Optional<Part> partOptional, URL url) {

        return null;
        //new XmlTypeLoader()
        //if (partOptional.isPresent()) {
        //    Part part = partOptional.get();
        //    if (part.getElementName() != null) {
        //        final QName elementName = part.getElementName();
        //        final XmlMetaDataBuilder<?> createXmlObject = new DefaultMetaDataBuilder().createXmlObject(elementName);
        //        for (String schema : schemas) {
        //            createXmlObject.addSchemaStringList(schema);
        //        }
        //        createXmlObject.setEncoding(charset());
        //        createXmlObject.setSourceUri(url);
        //        return new DefaultMetaData(createXmlObject.build());
        //    } else if (part.getTypeName() != null) {
        //        DataType dataType = getDataTypeFromTypeName(part);
        //        DefaultSimpleMetaDataModel defaultSimpleMetaDataModel = new DefaultSimpleMetaDataModel(dataType);
        //        return new DefaultMetaData(defaultSimpleMetaDataModel);
        //    }
        //}
        //return new DefaultMetaData(new DefaultUnknownMetaDataModel());
    }



    //private void addProperties(MetaData metadata, OperationWsdlResolver invokeWsdlResolver, MetaDataPropertyScope metaDataPropertyScope) {
    //    final List<SOAPHeader> headers = invokeWsdlResolver.getOperationHeaders();
    //    for (SOAPHeader soapHeader : headers) {
    //        final Message message = invokeWsdlResolver.getDefinition().getMessage(soapHeader.getMessage());
    //        if (message != null) {
    //            final Part part = message.getPart(soapHeader.getPart());
    //            metadata.addProperty(metaDataPropertyScope, SoapkitConstants.SOAP_HEADERS_PROPERTY_PREFIX + part.getElementName().getLocalPart(),
    //                                 new DefaultXmlMetaDataModel(invokeWsdlResolver.getSchemas(), part.getElementName(), charset()));
    //        }
    //    }
    //}


    private Charset charset() {
        return Charset.defaultCharset();
    }

    private DataType getDataTypeFromTypeName(Part part) {
        String localPart = part.getTypeName().getLocalPart();

        Map<String, DataType> types = new HashMap<String, DataType>();
        types.put("string", DataType.STRING);
        types.put("boolean", DataType.BOOLEAN);
        types.put("date", DataType.DATE);
        types.put("decimal", DataType.DECIMAL);
        types.put("byte", DataType.BYTE);
        types.put("unsignedByte", DataType.BYTE);
        types.put("dateTime", DataType.DATE_TIME);
        types.put("int", DataType.INTEGER);
        types.put("integer", DataType.INTEGER);
        types.put("unsignedInt", DataType.INTEGER);
        types.put("short", DataType.INTEGER);
        types.put("unsignedShort", DataType.INTEGER);
        types.put("long", DataType.LONG);
        types.put("unsignedLong", DataType.LONG);
        types.put("double", DataType.DOUBLE);

        DataType dataType = types.get(localPart);
        return dataType != null ? dataType : DataType.STRING;
    }
    //
    //@Override
    //public String toString()
    //{
    //    return "DefaultWsdlMetaDataResolver{" +
    //           "wsdlUrl='" + wsdlUrl + '\'' +
    //           ", serviceName='" + serviceName + '\'' +
    //           ", portName='" + portName + '\'' +
    //           ", operationName='" + operationName + '\'' +
    //           '}';
    //}
}
