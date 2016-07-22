/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api;

import java.util.LinkedHashMap;
import java.util.Map;

public class WSResponseAttributesBuilder
{

    private Integer statusCode;
    private String reasonPhrase;
    private Map<String, String> soapHeaders = new LinkedHashMap<>();

    public WSResponseAttributesBuilder setStatusCode(Integer statusCode)
    {
        this.statusCode = statusCode;
        return this;
    }

    public WSResponseAttributesBuilder setReasonPhrase(String reasonPhrase)
    {
        this.reasonPhrase = reasonPhrase;
        return this;
    }

    public WSResponseAttributesBuilder addHeader(String key, String value)
    {
        soapHeaders.put(key, value);
        return this;
    }

    public WSAttributes build()
    {
        return new WSAttributes(statusCode, reasonPhrase, soapHeaders);
    }


}