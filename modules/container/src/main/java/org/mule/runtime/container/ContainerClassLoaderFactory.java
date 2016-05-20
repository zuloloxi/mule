/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.runtime.container;

import org.mule.runtime.module.artifact.classloader.ArtifactClassLoader;
import org.mule.runtime.module.artifact.classloader.FilteringArtifactClassLoader;
import org.mule.runtime.module.artifact.classloader.MuleArtifactClassLoader;
import org.mule.runtime.module.artifact.classloader.MuleClassLoaderLookupPolicy;

import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

//TODO(pablo.kraan): make this to extend ArtifactClassLoaderFactory
public class ContainerClassLoaderFactory
{

    //TODO(pablo.kraan): MULE-9524: Add a way to configure system packages used on class loading lookup
    public static final Set<String> BOOT_PACKAGES = ImmutableSet.of(
            "java.", "javax.", "org.apache.xerces", "org.mule.mvel2",
            "org.apache.logging.log4j", "org.slf4j", "org.apache.commons.logging", "org.apache.log4j",
            //TODO(pablo.kraan): check why these apckages are required
            "org.dom4j", "org.w3c.dom", "com.sun", "sun", "org.springframework"

    );

    public static final Set<String> SYSTEM_PACKAGES = ImmutableSet.of(
            "org.mule.runtime", "com.mulesoft.mule.runtime"
    );

    public ArtifactClassLoader createContainerClassLoader(final ClassLoader parentClassLoader)
    {
        final Set<String> parentOnlyPackages = new HashSet<>(BOOT_PACKAGES);
        parentOnlyPackages.addAll(SYSTEM_PACKAGES);
        final MuleClassLoaderLookupPolicy containerLookupPolicy = new MuleClassLoaderLookupPolicy(Collections.emptyMap(), parentOnlyPackages);
        final ArtifactClassLoader containerClassLoader = new MuleArtifactClassLoader("mule", new URL[0], parentClassLoader, containerLookupPolicy) {
            @Override
            public URL findResource(String name)
            {
                // Container classLoader is just an adapter, must find resources on the parent
                return parentClassLoader.getResource(name);
            }

            @Override
            public Enumeration<URL> findResources(String name) throws IOException
            {
                // Container classLoader is just an adapter, must find resources on the parent
                return parentClassLoader.getResources(name);
            }
        };

        return containerClassLoader;
    }

    public FilteringArtifactClassLoader createContainerFilteringClassLoader(ArtifactClassLoader containerClassLoader)
    {
        return new FilteringContainerClassLoader(containerClassLoader, new ContainerClassLoaderFilterFactory().create(BOOT_PACKAGES));
    }
}
