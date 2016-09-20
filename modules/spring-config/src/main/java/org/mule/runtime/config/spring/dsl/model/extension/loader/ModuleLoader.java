/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.config.spring.dsl.model.extension.loader;

import org.mule.runtime.config.spring.XmlConfigurationDocumentLoader;
import org.mule.runtime.config.spring.dsl.model.ComponentModel;
import org.mule.runtime.config.spring.dsl.model.ComponentModelReader;
import org.mule.runtime.config.spring.dsl.model.ModuleModel;
import org.mule.runtime.config.spring.dsl.model.extension.ModuleExtension;
import org.mule.runtime.config.spring.dsl.processor.ConfigLine;
import org.mule.runtime.config.spring.dsl.processor.xml.XmlApplicationParser;
import org.mule.runtime.core.registry.SpiServiceRegistry;
import org.mule.runtime.core.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.Document;

public class ModuleLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(ModuleLoader.class);

  private Map<String, Optional<ModuleExtension>> extensions;

  public ModuleLoader() {
    this.extensions = new HashMap<>();
  }

  public Optional<ModuleExtension> lookup(final String location, final Properties properties) {
    return extensions.computeIfAbsent(location, s -> {
      try {
        return getModuleFor(location, properties);
      } catch (Exception e) {
        //do nothing
        return Optional.empty();
      }
    });
  }

  private Resource[] getModulesResources() throws IOException {
    PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
    return pathMatchingResourcePatternResolver.getResources("classpath*:**/module-*.xml");
  }

  ///**
  // * TODO WIP-OPERATIONS copied from MuleArtifactContext#getXmlDocument
  // */ //TODO WIP-OPERATIONS remove dead code
  //private static final int VALIDATION_XSD = 3;
  //
  //private Document getXmlDocument(Resource resource) throws Exception {
  //  String filename = resource.getFilename();
  //  InputStream inputStream = resource.getInputStream();
  //  MuleArtifactContext.MuleLoggerErrorHandler errorHandler = new MuleArtifactContext.MuleLoggerErrorHandler(filename);
  //  Document document = new MuleDocumentLoader()
  //      .loadDocument(new InputSource(inputStream),
  //                    new DelegatingEntityResolver(Thread.currentThread().getContextClassLoader()),
  //                    errorHandler, VALIDATION_XSD, true);
  //  errorHandler.displayErrors();
  //  return document;
  //}

  private Optional<ModuleExtension> getModuleFor(String schemaLocation, Properties applicationProperties) throws Exception {
    Optional<ModuleExtension> result = Optional.empty();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(String.format("Looking module under the following schema: %s ", schemaLocation));
    }

    XmlConfigurationDocumentLoader xmlConfigurationDocumentLoader = new XmlConfigurationDocumentLoader();

    for (Resource resource : this.getModulesResources()) {
      //Document moduleDocument = getXmlDocument(resource);
      //TODO WIP-OPERATIONS remove dead code
      Document moduleDocument = xmlConfigurationDocumentLoader.loadDocument(resource.getInputStream());
      XmlApplicationParser xmlApplicationParser = new XmlApplicationParser(new SpiServiceRegistry());
      Optional<ConfigLine> parseModule = xmlApplicationParser.parse(moduleDocument.getDocumentElement());
      if (!parseModule.isPresent()) {
        //throw new IllegalArgumentException(String.format("There was an issue while parsing the module for [%s]", resource.getFilename()));
        return result;
      }

      ComponentModelReader componentModelReader = new ComponentModelReader(applicationProperties);
      ComponentModel componentModel =
          componentModelReader.extractComponentDefinitionModel(parseModule.get(), resource.getFilename());

      if (componentModel.getIdentifier().equals(ModuleModel.MODULE_IDENTIFIER)
          && StringUtils.startsWith(schemaLocation,
                                    componentModel.getParameters().get(ModuleModel.MODULE_NAMESPACE_ATTRIBUTE))) {
        ModuleExtension moduleExtension = new ModuleModel().loadModule(componentModel);
        result = Optional.of(moduleExtension);
        break;
      }
    }
    return result;
  }
}
