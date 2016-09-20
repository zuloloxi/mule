/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.config.spring.dsl.model;

import static org.mule.runtime.config.spring.dsl.processor.xml.ModuleXmlNamespaceInfoProvider.MODULE_NAMESPACE_NAME;
import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.model.MetadataFormat;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.config.spring.dsl.model.extension.ModuleExtension;
import org.mule.runtime.config.spring.dsl.model.extension.OperationExtension;
import org.mule.runtime.config.spring.dsl.model.extension.ParameterExtension;
import org.mule.runtime.core.config.ComponentIdentifier;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ModuleModel {

  public static final String PARAMETER_NAME = "name";
  public static final String PARAMETER_DEFAULT_VALUE = "defaultValue";
  public static final String PARAMETER_TYPE = "type";

  public static final String MODULE_NAME = "name";
  public static final String MODULE_NAMESPACE_ATTRIBUTE = "namespace";
  public static final String MODULE_TAG = "module";

  public static final ComponentIdentifier OPERATION_IDENTIFIER =
      new ComponentIdentifier.Builder().withNamespace(MODULE_NAMESPACE_NAME).withName("operation").build();
  public static final ComponentIdentifier OPERATION_PROPERTY_IDENTIFIER =
      new ComponentIdentifier.Builder().withNamespace(MODULE_NAMESPACE_NAME).withName("property").build();
  public static final ComponentIdentifier OPERATION_PARAMETERS_IDENTIFIER =
      new ComponentIdentifier.Builder().withNamespace(MODULE_NAMESPACE_NAME).withName("parameters").build();
  public static final ComponentIdentifier OPERATION_PARAMETER_IDENTIFIER =
      new ComponentIdentifier.Builder().withNamespace(MODULE_NAMESPACE_NAME).withName("parameter").build();
  public static final ComponentIdentifier OPERATION_BODY_IDENTIFIER =
      new ComponentIdentifier.Builder().withNamespace(MODULE_NAMESPACE_NAME).withName("body").build();
  public static final ComponentIdentifier MODULE_IDENTIFIER =
      new ComponentIdentifier.Builder().withNamespace(ModuleModel.MODULE_TAG).withName(ModuleModel.MODULE_TAG).build();

  public static final String OPERATION_PARAM_PREFIX = "param.";
  public static final String OPERATION_PROPERTY_PREFIX = "property.";

  public ModuleExtension loadModule(ComponentModel moduleModel) {
    ModuleExtension moduleExtension = extractModuleExtension(moduleModel);
    return moduleExtension;
  }

  private ModuleExtension extractModuleExtension(ComponentModel moduleModel) {
    String name = moduleModel.getParameters().get(MODULE_NAME);
    String namespace = moduleModel.getParameters().get(MODULE_NAMESPACE_ATTRIBUTE);
    ModuleExtension moduleExtension = new ModuleExtension(name, namespace, moduleModel);
    moduleExtension.setProperties(loadPropertiesFrom(moduleModel));
    moduleExtension.setOperations(loadOperationsFrom(moduleModel));
    moduleExtension.setGlobalElements(loadGlobalElementsFrom(moduleModel));
    return moduleExtension;
  }

  private List<ComponentModel> loadGlobalElementsFrom(ComponentModel moduleModel) {
    return moduleModel.getInnerComponents().stream()
        .filter(child -> !child.getIdentifier().equals(OPERATION_PROPERTY_IDENTIFIER)
            && !child.getIdentifier().equals(OPERATION_IDENTIFIER))
        .collect(Collectors.toList());
  }

  private List<ParameterExtension> loadPropertiesFrom(ComponentModel moduleModel) {
    return moduleModel.getInnerComponents().stream()
        .filter(child -> child.getIdentifier().equals(OPERATION_PROPERTY_IDENTIFIER))
        .map(param -> extractParameter(param))
        .collect(Collectors.toList());
  }

  private Map<String, OperationExtension> loadOperationsFrom(ComponentModel moduleModel) {
    return moduleModel.getInnerComponents().stream()
        .filter(child -> child.getIdentifier().equals(OPERATION_IDENTIFIER))
        .map(operationModel -> extractOperationExtension(operationModel))
        .collect(Collectors.toMap(OperationExtension::getName, Function.identity()));
  }

  private OperationExtension extractOperationExtension(ComponentModel operationModel) {
    OperationExtension operationExtension = new OperationExtension(operationModel.getNameAttribute(), operationModel);
    operationExtension.setParameters(extractOperationParameters(operationModel));
    return operationExtension;
  }

  private List<ParameterExtension> extractOperationParameters(ComponentModel componentModel) {
    List<ParameterExtension> parameters = Collections.emptyList();

    Optional<ComponentModel> optionalParametersComponentModel = componentModel.getInnerComponents()
        .stream()
        .filter(child -> child.getIdentifier().equals(OPERATION_PARAMETERS_IDENTIFIER)).findAny();
    if (optionalParametersComponentModel.isPresent()) {
      parameters = optionalParametersComponentModel.get().getInnerComponents()
          .stream()
          .filter(child -> child.getIdentifier().equals(OPERATION_PARAMETER_IDENTIFIER))
          .map(param -> extractParameter(param))
          .collect(Collectors.toList());
    }
    return parameters;
  }

  private ParameterExtension extractParameter(ComponentModel param) {
    Map<String, String> parameters = param.getParameters();
    return new ParameterExtension(parameters.get(PARAMETER_NAME),
                                  extractParameterType(parameters.get(PARAMETER_TYPE)),
                                  parameters.get(PARAMETER_DEFAULT_VALUE));
  }

  private MetadataType extractParameterType(String type) {
    BaseTypeBuilder<?> baseTypeBuilder = BaseTypeBuilder.create(MetadataFormat.JAVA);
    switch (type) {
      case "string":
        baseTypeBuilder.stringType();
        break;
      case "boolean":
        baseTypeBuilder.booleanType();
        break;
      case "datetime":
        baseTypeBuilder.dateTimeType();
        break;
      case "date":
        baseTypeBuilder.dateType();
        break;
      case "integer":
        baseTypeBuilder.numberType();
        break;
      case "time":
        baseTypeBuilder.timeType();
        break;
      default:
        throw new IllegalArgumentException(String.format(
                                                         "should not have reach here, supported types for <parameter>(simple) are string, boolean, datetime, date, number or time for now. Type obtained [%s]",
                                                         type));
    }
    return baseTypeBuilder.build();
  }
}
