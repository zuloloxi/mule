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
import static org.mule.test.metadata.extension.resolver.TestMultiLevelKeyResolver.AMERICA;
import static org.mule.test.metadata.extension.resolver.TestMultiLevelKeyResolver.ARGENTINA;
import static org.mule.test.metadata.extension.resolver.TestMultiLevelKeyResolver.BUENOS_AIRES;
import org.mule.runtime.api.metadata.ProcessorId;
import org.mule.runtime.api.metadata.descriptor.MetadataKeyDescriptor;
import org.mule.runtime.api.metadata.resolving.MetadataResult;

import java.util.List;

import org.junit.Test;

public class MultilevelMetadataKeysTestCase extends MetadataExtensionFunctionalTestCase
{
    @Test
    public void resolveMultilevelKey() throws Exception
    {
        componentId = new ProcessorId(SIMPLE_MULTILEVEL_KEY_RESOLVER, FIRST_PROCESSOR_INDEX);
        final MetadataResult<List<MetadataKeyDescriptor>> metadataKeysResult = metadataManager.getMetadataKeys(componentId);
        assertThat(metadataKeysResult.isSuccess(), is(true));

        final List<MetadataKeyDescriptor> metadataKeys = metadataKeysResult.get();
        assertThat(metadataKeys.size(), is(2));

        MetadataKeyDescriptor america = metadataKeys.get(0);
        assertMultilevelKey(america);
    }

    private void assertMultilevelKey(MetadataKeyDescriptor america)
    {
        assertThat(america.isPartial(), is(false));
        assertThat(america.getKey().getChilds().isPresent(), is(true));
        assertThat(america.getKey().getChilds().get(), hasSize(2));
    }

    @Test
    public void elementsAreStoredInCaches() throws Exception
    {
        Object payload = runFlow(PARTIAL_MULTILEVEL_KEY_RESOLVER).getMessage().getPayload();
        assertThat(payload, is(String.format("%s|%s|%s",AMERICA, ARGENTINA, BUENOS_AIRES)));
    }

    @Test
    public void incompletePartialMultilevelKey()
    {
        componentId = new ProcessorId(PARTIAL_MULTILEVEL_KEY_RESOLVER, FIRST_PROCESSOR_INDEX);
        final MetadataResult<List<MetadataKeyDescriptor>> metadataKeysResult = metadataManager.getMetadataKeys(componentId);
        assertThat(metadataKeysResult.isSuccess(), is(true));
        final List<MetadataKeyDescriptor> metadataKeys = metadataKeysResult.get();
        assertThat(metadataKeys.size(), is(2));
        assertThat(metadataKeys.get(0).isPartial(), is(true));
        assertThat(metadataKeys.get(1).isPartial(), is(true));
    }

    @Test
    public void resolvePartialMultilevelKey() throws Exception
    {
        componentId = new ProcessorId(PARTIAL_MULTILEVEL_KEY_RESOLVER, FIRST_PROCESSOR_INDEX);
        final MetadataResult<List<MetadataKeyDescriptor>> metadataKeysResult = metadataManager.getMetadataKeys(componentId);
        assertThat(metadataKeysResult.isSuccess(), is(true));

        final List<MetadataKeyDescriptor> metadataKeys = metadataKeysResult.get();
        assertThat(metadataKeys.size(), is(2));

        final MetadataResult<MetadataKeyDescriptor> keyDescriptor = metadataManager.getMetadataKeyChilds(componentId, metadataKeys.get(0).getKey());
        assertThat(keyDescriptor.isSuccess(), is(true));
        assertThat(keyDescriptor.get().isPartial(), is(false));
    }

}
