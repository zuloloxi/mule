/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws;


import static java.util.Collections.emptyMap;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.mule.extension.ws.SoapFaultCodeMatcher.hasFaultCode;
import org.mule.extension.ws.api.WSConsumer;
import org.mule.extension.ws.internal.exception.SoapFaultException;
import org.mule.functional.junit4.ExtensionFunctionalTestCase;
import org.mule.runtime.core.api.MuleMessage;
import org.mule.tck.junit4.rule.DynamicPort;

import java.io.Serializable;
import java.util.Map;

import org.junit.Rule;
import org.junit.rules.ExpectedException;

public abstract class AbstractWSConsumerFunctionalTestCase extends ExtensionFunctionalTestCase
{

    @Rule
    public DynamicPort dynamicPort = new DynamicPort("port");
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    protected static final String ECHO_REQUEST = "<tns:echo xmlns:tns=\"http://consumer.ws.extension.mule.org/\">" +
                                                 "<text>Hello</text></tns:echo>";

    protected static final String EXPECTED_ECHO_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                                                           "<ns2:echoResponse xmlns:ns2=\"http://consumer.ws.extension.mule.org/\">" +
                                                           "<text>Hello</text></ns2:echoResponse>";

    @Override
    protected Class<?>[] getAnnotatedExtensionClasses()
    {
        return new Class<?>[] {WSConsumer.class};
    }

    protected void assertValidResponse(String flowName) throws Exception
    {
        assertValidResponse(flowName, ECHO_REQUEST, emptyMap());
    }

    protected void assertValidResponse(String flowName, Object payload, Map<String, Serializable> properties) throws Exception
    {
        MuleMessage response = flowRunner(flowName).withPayload(payload).withInboundProperties(properties).run().getMessage();
        assertXMLEqual(EXPECTED_ECHO_RESPONSE, getPayloadAsString(response));
    }

    protected void assertSoapFault(String flowName, String expectedFaultCode) throws Exception
    {
        assertSoapFault(flowName, ECHO_REQUEST, expectedFaultCode);
    }

    protected void assertSoapFault(String flowName, String message, String expectedFaultCode) throws Exception
    {
        assertSoapFault(flowName, message, emptyMap(), expectedFaultCode);
    }

    protected void assertSoapFault(String flowName, String message, Map<String, Serializable> properties, String expectedFaultCode) throws Exception
    {
        expectedException.expect(SoapFaultException.class);
        expectedException.expect(hasFaultCode(expectedFaultCode));
        flowRunner(flowName).withPayload(message).withInboundProperties(properties).run().getMessage();
    }

}
