/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.metadata;

import static java.util.stream.Collectors.toSet;
import static org.mule.runtime.api.metadata.descriptor.builder.MetadataDescriptorBuilder.keyDescriptor;
import org.mule.runtime.api.metadata.DefaultMetadataKey;
import org.mule.runtime.api.metadata.MetadataKey;
import org.mule.runtime.api.metadata.MetadataProperty;
import org.mule.runtime.api.metadata.descriptor.MetadataKeyDescriptor;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class MetadataKeyWrapper implements MetadataKey
{

    private MetadataKey key;
    private Set<MetadataKey> childs;
    private int level;
    private final int metadataKeyIds;

    public MetadataKeyWrapper(MetadataKey key, int metadataKeyIds)
    {
        this(key, 1, metadataKeyIds);
    }

    private MetadataKeyWrapper(MetadataKey key, int level, int metadataKeyIds)
    {
        this.key = key;
        this.level = level;
        this.metadataKeyIds = metadataKeyIds;
        childs = key.getChilds().isPresent() ?
                 key.getChilds().get().stream().map(k -> new MetadataKeyWrapper(k, level + 1, metadataKeyIds)).collect(toSet()) : null;
    }

    public MetadataKeyDescriptor describe()
    {
        return keyDescriptor(new DefaultMetadataKey(key.getId(),
                                                    key.getDisplayName(),
                                                    key.getProperties(),
                                                    level == metadataKeyIds ? null : new HashSet<>()))
                .isPartial(!isComplete())
                .build();
    }

    public boolean isComplete()
    {
        if (metadataKeyIds == level)
        {
            return true;
        }

        // When is leaf but not the maxLevel
        if (childs == null)
        {
            return true;
        }

        return !childs.isEmpty() && !childs.stream().anyMatch(k -> !((MetadataKeyWrapper)k).isComplete());

    }

    @Override
    public String getId()
    {
        return key.getId();
    }

    @Override
    public String getDisplayName()
    {
        return key.getDisplayName();
    }

    @Override
    public Optional<Set<MetadataKey>> getChilds()
    {
        return Optional.ofNullable(childs);
    }

    @Override
    public <T extends MetadataProperty> Optional<T> getMetadataProperty(Class<T> propertyType)
    {
        return key.getMetadataProperty(propertyType);
    }

    @Override
    public Set<MetadataProperty> getProperties()
    {
        return key.getProperties();
    }

    MetadataKeyWrapper withNewLevel(List<MetadataKey> newChilds)
    {
        if (childs == null || childs.isEmpty())
        {
            childs = newChilds.stream().map(k -> new MetadataKeyWrapper(k, level + 1, metadataKeyIds)).collect(toSet());
            return new MetadataKeyWrapper(this, metadataKeyIds);
        }

        Optional<MetadataKeyWrapper> first = childs.stream().map(child -> ((MetadataKeyWrapper)child).withNewLevel(newChilds)).findFirst();
        return first.orElse(new MetadataKeyWrapper(this, metadataKeyIds));
    }

    List<String> orderedValues()
    {
        LinkedList<String> values = new LinkedList<>();

        if (childs == null || childs.isEmpty() || childs.size() > 1)
        {
            values.add(key.getId());
            return values;
        }

        Optional<MetadataKey> first = childs.stream().findFirst();
        if(first.isPresent())
        {
            values.addAll(((MetadataKeyWrapper)first.get()).orderedValues());
            values.add(0, key.getId());
            return values;
        }

        return Collections.emptyList();
    }
}