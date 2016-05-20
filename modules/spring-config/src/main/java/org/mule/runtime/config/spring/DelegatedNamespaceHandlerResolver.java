/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.runtime.config.spring;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.util.Assert;

public class DelegatedNamespaceHandlerResolver implements NamespaceHandlerResolver
{

    public static DelegatedNamespaceHandlerResolver instance = new DelegatedNamespaceHandlerResolver();

    /**
     * logger
     */
    private static final Log log = LogFactory.getLog(DelegatedNamespaceHandlerResolver.class);

    private final Map<NamespaceHandlerResolver, String> resolvers = new LinkedHashMap<NamespaceHandlerResolver, String>(
            2);


    public void addNamespaceHandler(NamespaceHandlerResolver resolver, String resolverToString)
    {
        Assert.notNull(resolver);
        resolvers.put(resolver, resolverToString);
    }

    public NamespaceHandler resolve(String namespaceUri)
    {
        boolean trace = log.isTraceEnabled();

        for (Iterator<Map.Entry<NamespaceHandlerResolver, String>> iterator = resolvers.entrySet().iterator(); iterator.hasNext(); )
        {
            Map.Entry<NamespaceHandlerResolver, String> entry = iterator.next();
            NamespaceHandlerResolver handlerResolver = entry.getKey();
            if (trace)
            {
                log.trace("Trying to resolve namespace [" + namespaceUri + "] through resolver " + entry.getValue());
            }
            NamespaceHandler handler = handlerResolver.resolve(namespaceUri);

            String resolvedMsg = (handler != null ? "" : "not ");
            if (trace)
            {
                log.trace("Namespace [" + namespaceUri + "] was " + resolvedMsg + "resolved through handler resolver "
                          + entry.getValue());
            }

            if (handler != null)
            {
                return handler;
            }

        }
        return null;
    }
}