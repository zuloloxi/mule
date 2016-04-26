/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.metadata;

import static org.mule.runtime.core.config.i18n.MessageFactory.createStaticMessage;
import static org.mule.runtime.module.extension.internal.util.IntrospectionUtils.checkInstantiable;
import static org.mule.runtime.module.extension.internal.util.IntrospectionUtils.getExposedFields;
import org.mule.runtime.core.api.MuleRuntimeException;
import org.mule.runtime.extension.api.annotation.metadata.MetadataKeyId;
import org.mule.runtime.extension.api.annotation.metadata.MetadataKeyPart;
import org.mule.runtime.extension.api.introspection.ComponentModel;
import org.mule.runtime.module.extension.internal.model.property.ImplementingMethodModelProperty;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


final class MetadataKeyIdArgumentResolver
{

    private final Class type;
    private final Map<Field, String> fieldValueMap = new HashMap<>();

    public MetadataKeyIdArgumentResolver(ComponentModel componentModel, List<String> values)
    {
        try
        {
            Optional<ImplementingMethodModelProperty> modelProperty = componentModel.getModelProperty(ImplementingMethodModelProperty.class);
            Optional<Parameter> any = Arrays.stream(modelProperty.get().getMethod().getParameters()).filter(p -> p.isAnnotationPresent(MetadataKeyId.class)).findAny();
            type = any.get().getType();
            checkInstantiable(type);

        }
        catch (Exception e)
        {
            throw new MuleRuntimeException(createStaticMessage("Could not create MetadataKeyIdResolver"), e);
        }

        for (Field keyPart : getExposedFields(type))
        {
            MetadataKeyPart metadataKeyPart = keyPart.getAnnotation(MetadataKeyPart.class);
            if (metadataKeyPart != null && metadataKeyPart.order() <= values.size())
            {
                fieldValueMap.put(keyPart, values.get(metadataKeyPart.order() - 1));
            }
        }
    }

    public Object resolve()
    {
        if (type.equals(String.class))
        {
            return fieldValueMap.get(fieldValueMap.keySet().iterator().next());
        }

        try
        {
            Object metadataKeyId = type.newInstance();
            for (Field field : fieldValueMap.keySet())
            {
                field.setAccessible(true);
                field.set(metadataKeyId, fieldValueMap.get(field));
            }
            return metadataKeyId;
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            throw new MuleRuntimeException(createStaticMessage("Could not create composed MetadataKey"), e);
        }
    }

}
