/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.runtime.config.spring;

import org.mule.runtime.module.artifact.classloader.ArtifactClassLoader;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamespaceManager
{

    private static final Logger LOGGER = LoggerFactory.getLogger(NamespaceManager.class);

    private final ArtifactClassLoader artifactClassLoader;

    /** The set of all namespace plugins known to the extender */
    //private NamespacePlugins namespacePlugins;

    /**
     * ServiceRegistration object returned by OSGi when registering the NamespacePlugins instance as a service
     */
    //private ServiceRegistration nsResolverRegistration, enResolverRegistration = null;

    private final String extenderInfo;

    private static final String META_INF = "META-INF/";

    private static final String SPRING_HANDLERS = "spring.handlers";

    private static final String SPRING_SCHEMAS = "spring.schemas";

    public NamespaceManager(ArtifactClassLoader artifactClassLoader)
    {
        this.artifactClassLoader = artifactClassLoader;
        this.extenderInfo = artifactClassLoader.getArtifactName();

        registerAvailableNamespaces(artifactClassLoader);
    }

    //TODO(pablo.kraan): fix all javadocs

    /**
     * Registers the namespace plugin handler if this bundle defines handler mapping or schema mapping resources.
     * <p>
     * <p/> This method considers only the bundle space and not the class space.
     */
    public void registerAvailableNamespaces(ArtifactClassLoader artifactClassLoader)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.trace("Inspecting artifact" + artifactClassLoader + " for Spring namespaces");
        }

        if (hasSpringHandlers(artifactClassLoader) || hasSpringSchemas(artifactClassLoader))
        {
            NamespacePlugins.instance.addPlugin(artifactClassLoader);
        }
    }

    private boolean hasSpringHandlers(ArtifactClassLoader artifactClassLoader)
    {
        return hasResources(artifactClassLoader, META_INF + SPRING_HANDLERS);
    }

    private boolean hasSpringSchemas(ArtifactClassLoader artifactClassLoader)
    {
        return hasResources(artifactClassLoader, META_INF + SPRING_SCHEMAS);
    }

    private boolean hasResources(ArtifactClassLoader artifactClassLoader, String resourceName)
    {
        boolean hasResource = false;
        try
        {
            final Enumeration<URL> resources = artifactClassLoader.findResources(resourceName);
            hasResource = resources.hasMoreElements();

            if (LOGGER.isDebugEnabled())
            {
                StringBuilder builder = new StringBuilder();
                builder.append("Spring resources found on ").append(artifactClassLoader.getArtifactName()).append(":\n");
                while (resources.hasMoreElements())
                {
                    final URL url = resources.nextElement();
                    builder.append(url).append("\n");
                }
                LOGGER.debug(builder.toString());
            }
        }
        catch (IOException e)
        {
            // Ignore
        }

        return hasResource;
    }
}