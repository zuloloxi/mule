/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.metadata;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.api.metadata.MetadataKeyBuilder.newKey;
import static org.mule.test.metadata.extension.resolver.TestMultiLevelKeyResolver.AMERICA;
import static org.mule.test.metadata.extension.resolver.TestMultiLevelKeyResolver.ARGENTINA;
import static org.mule.test.metadata.extension.resolver.TestMultiLevelKeyResolver.BUENOS_AIRES;
import static org.mule.test.metadata.extension.resolver.TestMultiLevelKeyResolver.LA_PLATA;
import static org.mule.test.metadata.extension.resolver.TestMultiLevelKeyResolver.SAN_FRANCISCO;
import static org.mule.test.metadata.extension.resolver.TestMultiLevelKeyResolver.USA;
import static org.mule.test.metadata.extension.resolver.TestMultiLevelKeyResolver.USA_DISPLAY_NAME;
import org.mule.runtime.api.metadata.MetadataKey;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class MetadataKeyWrapperTestCase
{

    @Test
    public void completeMultiLevelKey()
    {
        MetadataKey key = newKey(AMERICA)
                .withDisplayName(AMERICA)
                .withChild(newKey(ARGENTINA)
                                   .withChild(newKey(BUENOS_AIRES))
                                   .withChild(newKey(LA_PLATA)))
                .withChild(newKey(USA)
                                   .withDisplayName(USA_DISPLAY_NAME)
                                   .withChild(newKey(SAN_FRANCISCO)))
                .build();

        MetadataKeyWrapper keyWrapper = new MetadataKeyWrapper(key, 3);

        assertThat(keyWrapper.isComplete(), is(true));
    }

    @Test
    public void completeSingleLevelKey()
    {
        MetadataKey key = newKey(AMERICA)
                .withDisplayName(AMERICA)
                .build();

        MetadataKeyWrapper keyWrapper = new MetadataKeyWrapper(key, 1);

        assertThat(keyWrapper.isComplete(), is(true));
    }

    @Test
    public void incompleteSingleLevelKey()
    {
        MetadataKey key = newKey(AMERICA)
                .withDisplayName(AMERICA)
                .build();

        MetadataKeyWrapper keyWrapper = new MetadataKeyWrapper(key, 3);

        assertThat(keyWrapper.isComplete(), is(false));
    }

    @Test
    public void incompleteMultiLevelKey()
    {
        MetadataKey key = newKey(AMERICA)
                .withDisplayName(AMERICA)
                .withChild(newKey(ARGENTINA)
                                   .withChild(newKey(BUENOS_AIRES))
                                   .withChild(newKey(LA_PLATA)))
                .withChild(newKey(USA))
                .build();

        MetadataKeyWrapper keyWrapper = new MetadataKeyWrapper(key, 3);

        assertThat(keyWrapper.isComplete(), is(false));

    }

    @Test
    public void keyWithOptionalLevels()
    {
        MetadataKey key = newKey(AMERICA)
                .withDisplayName(AMERICA)
                .withChild(newKey(ARGENTINA)
                                   .withChild(newKey(BUENOS_AIRES))
                                   .withChild(newKey(LA_PLATA)))
                .withChild(newKey(USA).noChilds())
                .build();

        MetadataKeyWrapper keyWrapper = new MetadataKeyWrapper(key, 3);

        assertThat(keyWrapper.isComplete(), is(true));
    }

    @Test
    public void withNewLevel()
    {
        MetadataKey key = newKey(AMERICA)
                .withDisplayName(AMERICA)
                    .withChild(newKey(ARGENTINA))
                .build();

        MetadataKeyWrapper keyWrapper = new MetadataKeyWrapper(key, 3);

        keyWrapper.withNewLevel(Arrays.asList(newKey(BUENOS_AIRES).build(), newKey(LA_PLATA).build()));

        assertThat(keyWrapper.isComplete(), is(true));

        // dale mas test
    }


    @Test
    public void values()
    {
        MetadataKey key = newKey(AMERICA)
                .withDisplayName(AMERICA)
                .withChild(newKey(ARGENTINA).withChild(newKey(BUENOS_AIRES)))
                .build();

        MetadataKeyWrapper keyWrapper = new MetadataKeyWrapper(key, 3);

        List<String> values = keyWrapper.orderedValues();


        assertThat(values, hasSize(3));
        // dale mas test
    }

}