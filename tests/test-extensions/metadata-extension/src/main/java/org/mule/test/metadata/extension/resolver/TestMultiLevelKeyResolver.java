/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.metadata.extension.resolver;

import static org.mule.runtime.api.metadata.MetadataKeyBuilder.newKey;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataKey;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.metadata.resolving.MetadataContentResolver;
import org.mule.runtime.api.metadata.resolving.MetadataKeysResolver;

import java.util.Arrays;
import java.util.List;

public class TestMultiLevelKeyResolver<T> implements MetadataKeysResolver<T>, MetadataContentResolver<MetadataKey>
{
    // continents
    public static final String AMERICA = "AMERICA";
    public static final String EUROPE = "EUROPE";

    // countries
    public static final String FRANCE = "FRANCE";
    public static final String ARGENTINA = "ARGENTINA";
    public static final String USA = "USA";
    public static final String USA_DISPLAY_NAME = "United States";

    // cities
    public static final String BUENOS_AIRES = "BA";
    public static final String LA_PLATA = "LPLT";
    public static final String PARIS = "PRS";
    public static final String SAN_FRANCISCO = "SFO";


    @Override
    public MetadataType getContentMetadata(MetadataContext context, MetadataKey key) throws MetadataResolvingException, ConnectionException
    {
        return null;
    }

    @Override
    public List<MetadataKey> getMetadataKeys(MetadataContext context) throws MetadataResolvingException, ConnectionException
    {
        MetadataKey america = newKey(AMERICA)
                .withDisplayName(AMERICA)
                .withChild(newKey(ARGENTINA)
                            .withChild(newKey(BUENOS_AIRES))
                            .withChild(newKey(LA_PLATA)))
                .withChild(newKey(USA)
                            .withDisplayName(USA_DISPLAY_NAME)
                            .withChild(newKey(SAN_FRANCISCO)))
                .build();

        MetadataKey europe = newKey(EUROPE)
                .withDisplayName(EUROPE)
                .withChild(newKey(FRANCE).withChild(newKey(PARIS)))
                .build();

        return Arrays.asList(america, europe);
    }
}
