/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.config.spring.dsl.model;

import static org.mule.runtime.config.spring.dsl.model.ApplicationModel.MULE_DOMAIN_ROOT_ELEMENT;
import static org.mule.runtime.config.spring.dsl.model.ApplicationModel.MULE_ROOT_ELEMENT;
import static org.mule.runtime.config.spring.dsl.model.ApplicationModel.SPRING_PROPERTY_IDENTIFIER;
import static org.mule.runtime.config.spring.dsl.model.ApplicationModel.VALUE_ATTRIBUTE;
import static org.mule.runtime.config.spring.dsl.processor.xml.CoreXmlNamespaceInfoProvider.CORE_NAMESPACE_NAME;
import static org.mule.runtime.config.spring.dsl.processor.xml.XmlCustomAttributeHandler.from;
import static org.mule.runtime.config.spring.dsl.processor.xml.XmlCustomAttributeHandler.to;
import org.mule.runtime.config.spring.dsl.processor.ConfigLine;
import org.mule.runtime.config.spring.dsl.processor.SimpleConfigAttribute;
import org.mule.runtime.core.config.ComponentIdentifier;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.springframework.util.PropertyPlaceholderHelper;

public class ComponentModelReader {

  private PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper("${", "}");
  private Properties applicationProperties;

  public ComponentModelReader(Properties applicationProperties) {
    this.applicationProperties = applicationProperties;
  }

  public ComponentModel extractComponentDefinitionModel(ConfigLine configLine, String configFileName) {

    String namespace = configLine.getNamespace() == null ? CORE_NAMESPACE_NAME : configLine.getNamespace();
    ComponentModel.Builder builder = new ComponentModel.Builder()
        .setIdentifier(new ComponentIdentifier.Builder().withNamespace(namespace).withName(configLine.getIdentifier()).build())
        .setTextContent(configLine.getTextContent());
    to(builder).addNode(from(configLine).getNode()).addConfigFileName(configFileName);
    for (SimpleConfigAttribute simpleConfigAttribute : configLine.getConfigAttributes().values()) {
      builder.addParameter(simpleConfigAttribute.getName(), resolveValueIfIsPlaceHolder(simpleConfigAttribute.getValue()),
                           simpleConfigAttribute.isValueFromSchema());
    }

    List<ComponentModel> componentModels = configLine.getChildren().stream()
        .map(childConfigLine -> extractComponentDefinitionModel(childConfigLine, configFileName))
        .collect(Collectors.toList());
    componentModels.stream().forEach(componentDefinitionModel -> {
      if (SPRING_PROPERTY_IDENTIFIER.equals(componentDefinitionModel.getIdentifier())) {
        String value = componentDefinitionModel.getParameters().get(VALUE_ATTRIBUTE);
        if (value != null) {
          builder.addParameter(componentDefinitionModel.getNameAttribute(), resolveValueIfIsPlaceHolder(value), false);
        }
      }
      builder.addChildComponentModel(componentDefinitionModel);
    });
    ConfigLine parent = configLine.getParent();
    if (parent != null && isConfigurationTopComponent(parent)) {
      builder.markAsRootComponent();
    }
    ComponentModel componentModel = builder.build();
    for (ComponentModel innerComponentModel : componentModel.getInnerComponents()) {
      innerComponentModel.setParent(componentModel);
    }
    return componentModel;
  }

  private String resolveValueIfIsPlaceHolder(String value) {
    return propertyPlaceholderHelper.replacePlaceholders(value, applicationProperties);
  }

  private boolean isConfigurationTopComponent(ConfigLine parent) {
    return (parent.getIdentifier().equals(MULE_ROOT_ELEMENT) || parent.getIdentifier().equals(MULE_DOMAIN_ROOT_ELEMENT));
  }
}
