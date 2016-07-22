/**
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
/**
 * (c) 2003-2014 MuleSoft, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package org.mule.extension.ws.api.metadata.utils;


import com.ibm.wsdl.extensions.schema.SchemaSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.mime.MIMEPart;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;

public class WsdlUtils {

    @SuppressWarnings("unchecked")
    public static String[] getServiceNames(Definition wsdlDefinition) {
        List<String> serviceNames = new ArrayList<String>();

        if (wsdlDefinition != null) {
            Map<QName, Service> services = wsdlDefinition.getServices();
            if (services != null) {
                for (QName name : services.keySet()) {
                    serviceNames.add(name.getLocalPart());
                }
            }
        }

        return serviceNames.toArray(new String[serviceNames.size()]);
    }

    @SuppressWarnings("unchecked")
    public static String[] getPortNames(Service service) {
        List<String> names = new ArrayList<String>();
        if (service != null && service.getPorts() != null) {
            names.addAll(service.getPorts().keySet());
        }
        return names.toArray(new String[names.size()]);
    }

    @SuppressWarnings("unchecked")
    public static String[] getOperationNames(Port port) {
        List<String> operationNames = new ArrayList<String>();

        List<BindingOperation> bindingOperations = (List<BindingOperation>) port.getBinding().getBindingOperations();
        for (BindingOperation operation : bindingOperations) {
            operationNames.add(operation.getName());
        }

        return operationNames.toArray(new String[operationNames.size()]);
    }

    public static Service getService(Definition def, String serviceName) {
        validateBlankString(serviceName, "service name");
        Service service = def.getService(new QName(def.getTargetNamespace(), serviceName));
        validateNotNull(service, "The service name ["+serviceName+"] was not found in the current wsdl file.");
        return service;
    }

    public static Port getPort(Service service, String portName) {
        validateBlankString(portName, "port name");
        Port port = service.getPort(portName.trim());
        validateNotNull(port, "The port name ["+ portName +"] was not found in the current wsdl file.");
        return port;
    }

    public static Operation getOperation(PortType portType, String operationName)
    {
        validateBlankString(operationName, "operation name");
        Operation operation = portType.getOperation(operationName, null, null);
        validateNotNull(operation, "The operation name ["+operationName+"] was not found in the current wsdl file.");
        return operation;
    }

    public static Fault getFault(Operation operation, String faultName){
        validateBlankString(faultName, "fault name");
        Fault fault = operation.getFault(faultName);
        validateNotNull(fault, "The fault name ["+faultName+"] was not found in the current wsdl file.");
        return fault;
    }

    /**
     * Given a Wsdl location (either local or remote) it will fetch the definition. If the definition cannot be created, then
     * an exception will be raised
     * @param wsdlLocation path to the desired Wsdl file
     * @return the Definiton that represents the Wsdl file
     */
    public static Definition parseWSDL(final String wsdlLocation) {
        try {
            validateBlankString(wsdlLocation, "wsdl location");

            WSDLFactory factory = WSDLFactory.newInstance();
            ExtensionRegistry registry = factory.newPopulatedExtensionRegistry();
            registry.registerSerializer(Types.class,
                    new QName("http://www.w3.org/2001/XMLSchema", "schema"),
                    new SchemaSerializer());
            // these will replace whatever may have already been registered
            // in these places, but there's no good way to check what was
            // there before.
            QName header = new QName("http://schemas.xmlsoap.org/wsdl/soap/", "header");
            registry.registerDeserializer(MIMEPart.class,
                    header,
                    registry.queryDeserializer(BindingInput.class, header));
            registry.registerSerializer(MIMEPart.class,
                    header,
                    registry.querySerializer(BindingInput.class, header));
            // get the original classname of the SOAPHeader
            // implementation that was stored in the registry.
            Class<? extends ExtensibilityElement> clazz =
                    registry.createExtension(BindingInput.class, header).getClass();
            registry.mapExtensionTypes(MIMEPart.class, header, clazz);

            WSDLReader wsdlReader = factory.newWSDLReader();
            wsdlReader.setFeature("javax.wsdl.verbose", false);
            wsdlReader.setFeature("javax.wsdl.importDocuments", true);
            wsdlReader.setExtensionRegistry(registry);

            Definition definition = wsdlReader.readWSDL(wsdlLocation);
            validateNotNull(definition, "There was an issue while parsing the wsdl file for [" + wsdlLocation + "].");

            return definition;
        } catch (WSDLException e) {
            String errorMessage = "Something went wrong when parsing the wsdl file";
            errorMessage += StringUtils.isBlank(e.getMessage()) ? " for [" + wsdlLocation + "]" : ", full message " + e.getMessage();
            throw new IllegalArgumentException(errorMessage, e); //TODO tech debt: we should analyze the type of exception (missing or corrupted file) and thrown better exceptions
        }
    }

    public static void validateNotNull(Object paramValue, String errorMessage)
    {
        if (paramValue == null){
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public static void validateBlankString(String paramValue, String paramName)
    {
        if (StringUtils.isBlank(paramValue)){
            throw new IllegalArgumentException("The " + paramName + " can not be blank nor null.");
        }
    }

    public static boolean isValidUrl(String url)
    {
        return StringUtils.isNotBlank(url) && url.startsWith("http");
    }

}
