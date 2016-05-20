/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.runtime.config.spring;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ChainedEntityResolver  implements EntityResolver
{

    /** logger */
    private static final Log log = LogFactory.getLog(ChainedEntityResolver.class);

    private final Map<EntityResolver, String> resolvers = new LinkedHashMap<EntityResolver, String>(2);


    public void addEntityResolver(EntityResolver resolver, String resolverToString) {
        Assert.notNull(resolver);
        resolvers.put(resolver, resolverToString);
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
    {
        boolean trace = log.isTraceEnabled();

        for (Map.Entry<EntityResolver, String> entry : resolvers.entrySet()) {
            EntityResolver entityResolver = entry.getKey();
            if (trace)
                log.trace("Trying to resolve entity [" + publicId + "|" + systemId + "] through resolver "
                          + entry.getValue());
            InputSource entity = entityResolver.resolveEntity(publicId, systemId);

            String resolvedMsg = (entity != null ? "" : "not ");
            if (trace)
                log.trace("Entity [" + publicId + "|" + systemId + "] was " + resolvedMsg
                          + "resolved through entity resolver " + entry.getValue());

            if (entity != null) {
                return entity;
            }
        }
        return null;
    }
}
