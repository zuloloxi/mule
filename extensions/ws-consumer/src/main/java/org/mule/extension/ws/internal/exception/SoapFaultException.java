/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.internal.exception;

import static org.mule.runtime.core.config.i18n.MessageFactory.createStaticMessage;
import org.mule.runtime.core.api.MessagingException;
import org.mule.runtime.core.api.MuleEvent;
import org.mule.runtime.core.api.processor.MessageProcessor;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * Exception thrown by the Web Services Consumer when processing a SOAP fault.
 * <p>
 * The exception contains the details about the SOAP fault.
 *
 * @since 3.6
 */
public class SoapFaultException extends MessagingException
{

    private final QName faultCode;
    private final QName subCode;
    private final Element detail;

    public SoapFaultException(MuleEvent event, QName faultCode, QName subCode, String message, Element detail, MessageProcessor failingMessageProcessor)
    {
        super(createStaticMessage(message), event, failingMessageProcessor);
        this.faultCode = faultCode;
        this.subCode = subCode;
        this.detail = detail;
    }

    public QName getFaultCode()
    {
        return faultCode;
    }

    public QName getSubCode()
    {
        return subCode;
    }

    public Element getDetail()
    {
        return detail;
    }
}
