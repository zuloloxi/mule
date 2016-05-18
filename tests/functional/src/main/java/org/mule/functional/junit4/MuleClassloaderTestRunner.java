/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.functional.junit4;

import static org.mule.functional.junit4.FunctionalTestCase.BOOT_PACKAGES;
import org.mule.runtime.container.ContainerClassLoaderFilterFactory;
import org.mule.runtime.container.FilteringContainerClassLoader;
import org.mule.runtime.module.artifact.classloader.ArtifactClassLoader;
import org.mule.runtime.module.artifact.classloader.MuleArtifactClassLoader;
import org.mule.runtime.module.artifact.classloader.MuleClassLoaderLookupPolicy;

import com.google.common.collect.ImmutableSet;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class MuleClassloaderTestRunner extends BlockJUnit4ClassRunner
{

    public MuleClassloaderTestRunner(Class<?> clazz) throws InitializationError
    {
        super(getFromTestClassloader(clazz));
    }

    private static Class<?> getFromTestClassloader(Class<?> clazz) throws InitializationError {
        try {
            ClassLoader testClassLoader = getExecutionClassLoader();
            return Class.forName(clazz.getName(), true, testClassLoader);
        } catch (ClassNotFoundException e) {
            throw new InitializationError(e);
        }
    }

    //TODO(pablo.kraan): move this constant to another place in container module
    public static final Set<String> SYSTEM_PACKAGES = ImmutableSet.of(
             "org.mule.runtime", "com.mulesoft.mule.runtime"
    );

    protected static ClassLoader getExecutionClassLoader()
    {
        final Set<String> parentOnlyPackages = new HashSet<>(BOOT_PACKAGES);
        parentOnlyPackages.addAll(SYSTEM_PACKAGES);

        final MuleClassLoaderLookupPolicy containerLookupPolicy = new MuleClassLoaderLookupPolicy(Collections.emptyMap(), parentOnlyPackages);
        final ArtifactClassLoader containerClassLoader = new MuleArtifactClassLoader("mule", new URL[0], null, containerLookupPolicy);

        return  new FilteringContainerClassLoader(containerClassLoader, new ContainerClassLoaderFilterFactory().create(BOOT_PACKAGES));
    }

}