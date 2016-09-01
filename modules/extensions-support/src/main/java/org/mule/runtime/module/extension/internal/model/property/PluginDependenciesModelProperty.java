/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.model.property;

import org.mule.runtime.extension.api.introspection.ModelProperty;

import java.util.List;

/**
 * An immutable model property which specifies all the extension plugin dependencies names.
 *
 * @since 4.0
 */
public final class PluginDependenciesModelProperty implements ModelProperty {

  private final List<String> dependencies;

  /**
   * Creates a new instance
   *
   * @param dependencies the names of the extension plugin dependencies.
   */
  public PluginDependenciesModelProperty(List<String> dependencies) {
    this.dependencies = dependencies;
  }

  /**
   * @return a {@link List} with all the plugin dependencies names.
   */
  public List<String> getPluginDependencies() {
    return dependencies;
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code pluginDependencies}
   */
  @Override
  public String getName() {
    return "pluginDependencies";
  }

  /**
   * {@inheritDoc}
   *
   * @return {@code false}
   */
  @Override
  public boolean isExternalizable() {
    return false;
  }
}
