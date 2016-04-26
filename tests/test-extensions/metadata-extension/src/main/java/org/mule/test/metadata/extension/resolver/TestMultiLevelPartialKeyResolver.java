/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.metadata.extension.resolver;

import static org.mule.runtime.api.metadata.MetadataKeyBuilder.newKey;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataKey;
import org.mule.runtime.api.metadata.MetadataKeyBuilder;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.test.metadata.extension.LocationKey;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TestMultiLevelPartialKeyResolver extends TestMultiLevelKeyResolver<LocationKey>
{

    @Override
    public List<MetadataKey> getMetadataKeys(MetadataContext context) throws MetadataResolvingException, ConnectionException
    {
        MetadataKey america = newKey(AMERICA)
                .withDisplayName(AMERICA)
                .build();

        MetadataKey europe = newKey(EUROPE)
                .withDisplayName(EUROPE)
                .build();

        return Arrays.asList(europe, america);
    }

    @Override
    public Optional<List<MetadataKey>> getMetadataKeyChilds(MetadataContext context, LocationKey partial) throws MetadataResolvingException, ConnectionException
    {
        MetadataKeyBuilder metadataKeyBuilder = newKey(ARGENTINA)
                .withChild(newKey(BUENOS_AIRES))
                .withChild(newKey(LA_PLATA));

        return Optional.of(Collections.singletonList(metadataKeyBuilder.build()));
    }

}
