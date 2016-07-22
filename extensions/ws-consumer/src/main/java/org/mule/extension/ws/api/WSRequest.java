/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api;

import java.util.Map;

import javax.activation.DataHandler;

public class WSRequest
{
    private Map<String, String> headers;

    private String body;
    private Map<String, DataHandler> attachments;

    public WSRequest(Map<String, String> headers, String body, Map<String, DataHandler> attachments)
    {
        this.headers = headers;
        this.body = body;
        this.attachments = attachments;
    }

    public Map<String, String> getHeaders()
    {
        return headers;
    }

    public void setHeaders(Map<String, String> headers)
    {
        this.headers = headers;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }

    public Map<String, DataHandler> getAttachments()
    {
        return attachments;
    }
}
