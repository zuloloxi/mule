/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api;

import org.mule.runtime.api.message.Attributes;

import java.util.Map;

public class WSAttributes implements Attributes
{


    // This should probably be removed
    private final Integer statusCode;
    // This should probably be removed
    private final String reasonPhrase;


    // Map?
    private Map<String, String> soapHeaders;

    public WSAttributes(Integer statusCode,
                        String reasonPhrase,
                        Map<String, String> soapHeaders)
    {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.soapHeaders = soapHeaders;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public String getReasonPhrase()
    {
        return reasonPhrase;
    }

    public Map<String, String> getSoapHeaders()
    {
        return soapHeaders;
    }
}
