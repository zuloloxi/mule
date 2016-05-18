/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.runtime.container;

import static org.mule.runtime.module.artifact.classloader.ArtifactClassLoaderFilter.EXPORTED_CLASS_PACKAGES_PROPERTY;
import static org.mule.runtime.module.artifact.classloader.ArtifactClassLoaderFilter.EXPORTED_RESOURCE_PACKAGES_PROPERTY;
import org.mule.runtime.core.config.bootstrap.BootstrapException;
import org.mule.runtime.core.config.bootstrap.ClassPathRegistryBootstrapDiscoverer;
import org.mule.runtime.core.util.StringUtils;
import org.mule.runtime.module.artifact.classloader.ArtifactClassLoaderFilter;
import org.mule.runtime.module.artifact.classloader.ClassLoaderFilter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Creates a {@link ClassLoaderFilter} for the container filter what is exposed to
 * Mule artifacts.
 * <p/>
 * Filter is constructed searching for {code}mule-module.properties{code} files in the classpath
 * and then merging the corresponding packages and resources in a new filter.
 */
public class ContainerClassLoaderFilterFactory
{

    public ClassLoaderFilter create(Set<String> bootPackages)
    {
        final ClassPathRegistryBootstrapDiscoverer muleModulesDiscoverer = new ClassPathRegistryBootstrapDiscoverer("META-INF/mule-module.properties");

        Map<String, String> packages = new HashMap<>();
        Set<String> modules = new HashSet<>();

        Set<String> resources = new HashSet<>();
        //TODO(pablo.kraan): is OK to expose this to apps or they should be read from container code only?
        // Adds default SPI resource folder
        resources.add("/META-INF/services");
        try
        {
            for (Properties muleModule : muleModulesDiscoverer.discover())
            {
                final String moduleName = (String) muleModule.get("module.name");
                if (StringUtils.isEmpty(moduleName))
                {
                    throw new IllegalStateException("Mule-module.properties must contain module.name property");
                }

                if (modules.contains(moduleName))
                {
                      throw new IllegalStateException(String.format("Module '%s' was already defined", moduleName));
                }
                modules.add(moduleName);

                final String exportedPackagesProperty = (String) muleModule.get(EXPORTED_CLASS_PACKAGES_PROPERTY);
                if (!StringUtils.isEmpty(exportedPackagesProperty))
                {
                    for (String packageName : exportedPackagesProperty.split(","))
                    {
                        packageName = packageName.trim();
                        if (!StringUtils.isEmpty(packageName))
                        {
                            //TODO(pablo.kraan): re-add this check once all modules are properly defined
                            //final String originalModule = packages.get(packageName);
                            //if (originalModule != null)
                            //{
                            //    throw new IllegalStateException(String.format("Package '%s' defined in module '%s' was  already defined in module '%s'", packageName, moduleName, originalModule));
                            //}
                            packages.put(packageName, moduleName);
                        }
                    }
                }

                final String exportedResourcesProperty = (String) muleModule.get(EXPORTED_RESOURCE_PACKAGES_PROPERTY);
                if (!StringUtils.isEmpty(exportedResourcesProperty))
                {
                    for (String resource : exportedResourcesProperty.split(","))
                    {
                        if (!StringUtils.isEmpty(resource.trim()))
                        {
                            if (resource.startsWith("/"))
                            {
                                resource = resource.substring(1);
                            }
                            resources.add(resource);
                        }
                    }
                }
            }

        }
        catch (BootstrapException e)
        {
            throw new RuntimeException("Cannot discover mule modules", e);
        }

        final ArtifactClassLoaderFilter artifactClassLoaderFilter = new ArtifactClassLoaderFilter(packages.keySet(), resources);
        return new ContainerClassLoaderFilter(artifactClassLoaderFilter, bootPackages);
    }

    public static class ContainerClassLoaderFilter implements ClassLoaderFilter
    {

        private final ClassLoaderFilter moduleClassLoaderFilter;
        private final Set<String> bootPackages;

        public ContainerClassLoaderFilter(ClassLoaderFilter moduleClassLoaderFilter, Set<String> bootPackages)
        {
            this.moduleClassLoaderFilter = moduleClassLoaderFilter;
            this.bootPackages = bootPackages;
        }

        @Override
        public boolean exportsClass(String name)
        {

            boolean exported = moduleClassLoaderFilter.exportsClass(name);

            if (!exported)
            {
                for (String bootPackage : bootPackages)
                {
                    if (name.startsWith(bootPackage))
                    {
                        exported = true;
                        break;
                    }
                }

            }
            System.out.println("exportsClass: " + exported + " - " + name);
            return exported;
        }

        @Override
        public boolean exportsResource(String name)
        {
            final boolean exported = moduleClassLoaderFilter.exportsResource(name);
            System.out.println("exportsResource: " + exported + " - " + name);
            return exported;
        }
    }

}
