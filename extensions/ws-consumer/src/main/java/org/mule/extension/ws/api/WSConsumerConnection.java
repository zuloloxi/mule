/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api;

import static java.lang.String.format;
import static javax.wsdl.factory.WSDLFactory.newInstance;
import static org.mule.runtime.core.util.IOUtils.getResourceAsUrl;
import org.mule.extension.ws.api.security.SecurityStrategy;
import org.mule.extension.ws.api.security.WSSecurity;
import org.mule.extension.ws.internal.SoapVersion;
import org.mule.extension.ws.internal.exception.WSConsumerException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.core.util.Base64;
import org.mule.runtime.module.http.api.requester.HttpRequesterConfig;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.xml.namespace.QName;

import org.xml.sax.InputSource;

public class WSConsumerConnection
{
    private final WSSecurity security;
    private final HttpRequesterConfig httpRequesterConfig;
    private final Definition wsdlDefinition;
    private final Binding binding;
    private final URL wsdlUrl;
    private final String address;
    private final String wsdlLocation;

    public WSConsumerConnection(String wsdlLocation, String serviceName, String portName, List<SecurityStrategy> securityStrategies, String address, HttpRequesterConfig httpRequesterConfig)
    {
        this.wsdlLocation = wsdlLocation;
        this.security = new WSSecurity(securityStrategies);
        this.address = address;
        this.httpRequesterConfig = httpRequesterConfig;

        this.wsdlUrl = getResourceAsUrl(wsdlLocation, getClass());
        if (wsdlUrl == null)
        {
            throw new RuntimeException(format("Can't find wsdl at %s", wsdlLocation));
        }

        try
        {
            URLConnection urlConnection = wsdlUrl.openConnection();
            if (wsdlUrl.getUserInfo() != null)
            {
                urlConnection.setRequestProperty("Authorization", "Basic " + Base64.encodeBytes(wsdlUrl.getUserInfo().getBytes()));
            }

            wsdlDefinition = newInstance()
                                .newWSDLReader()
                                .readWSDL(wsdlUrl.toString(), new InputSource(urlConnection.getInputStream()));
        }
        catch (WSDLException | IOException e)
        {
            throw new WSConsumerException("Cannot read wsdl definition", e);
        }

        Service service = wsdlDefinition.getService(new QName(wsdlDefinition.getTargetNamespace(), serviceName));
        checkNotNull(service, "Service %s not found in WSDL", serviceName);

        Port port = service.getPort(portName);
        checkNotNull(port, "Port %s not found in WSDL", portName);

        binding = port.getBinding();
        checkNotNull(binding, "Port %s has no binding", portName);

    }

    private void checkNotNull(Object o, String errorMessage, Object... errorMessageArgs)
    {
        if (o == null)
        {
            throw new WSConsumerException(errorMessage, errorMessageArgs);
        }
    }

    public Binding getBinding()
    {
        return binding;
    }

    public Definition getWsdlDefinition()
    {
        return wsdlDefinition;
    }

    public ConnectionValidationResult isValid()
    {
        return ConnectionValidationResult.success();
    }

    public WSSecurity getSecurity()
    {
        return security;
    }

    public String getAddress()
    {
        return address;
    }

    public HttpRequesterConfig getHttpRequesterConfig()
    {
        return httpRequesterConfig;
    }

    /**
     * Retrieves the SOAP version of a WSDL binding, or null if it is not a SOAP binding.
     */
    public SoapVersion getSoapVersion()
    {
        List extensions = binding.getExtensibilityElements();
        for (Object extension : extensions)
        {
            if (extension instanceof SOAPBinding)
            {
                return SoapVersion.SOAP_11;
            }
            if (extension instanceof SOAP12Binding)
            {
                return SoapVersion.SOAP_12;
            }
        }
        return null;
    }

    public String getWsdlLocation()
    {
        return wsdlLocation;
    }
}
