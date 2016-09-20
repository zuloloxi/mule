/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.config.spring;

import org.mule.runtime.config.spring.dsl.model.extension.ModuleExtension;
import org.mule.runtime.config.spring.dsl.model.extension.loader.ModuleLoader;
import org.mule.runtime.config.spring.dsl.model.extension.schema.ModuleSchemaGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import org.springframework.beans.factory.xml.DelegatingEntityResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ModuleDelegatingEntityResolver implements EntityResolver {

  private final ModuleLoader moduleLoader;
  private final EntityResolver entityResolver;

  public ModuleDelegatingEntityResolver() {
    this.entityResolver = new DelegatingEntityResolver(Thread.currentThread().getContextClassLoader());
    this.moduleLoader = new ModuleLoader();
  }

  @Override
  public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
    InputSource inputSource = entityResolver.resolveEntity(publicId, systemId);
    if (inputSource == null) {
      inputSource = generateModuleXsd(publicId, systemId);
    }
    return inputSource;
  }

  private InputSource generateModuleXsd(String publicId, String systemId) {
    InputSource inputSource = null;
    Properties properties = new Properties(); //TODO WIP-OPERATIONS we might have to take this properties as the ApplicationModel#configurePropertyPlaceholderResolver(ArtifactConfig)
    Optional<ModuleExtension> moduleOptional = moduleLoader.lookup(systemId, properties);
    if (moduleOptional.isPresent()) {
      InputStream schema = new ModuleSchemaGenerator().getSchema(moduleOptional.get());
      inputSource = new InputSource(schema);
      inputSource.setPublicId(publicId);
      inputSource.setSystemId(systemId);
    }
    return inputSource;
  }
}
