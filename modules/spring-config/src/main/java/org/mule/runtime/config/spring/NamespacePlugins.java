/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.runtime.config.spring;

import org.mule.runtime.module.artifact.classloader.ArtifactClassLoader;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.DelegatingEntityResolver;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class NamespacePlugins implements NamespaceHandlerResolver, EntityResolver, DisposableBean
{

    //TODO(pablo.kraan): this must not be a singleton
    public static NamespacePlugins instance = new NamespacePlugins();

    /**
     * Wrapper class which implements both {@link EntityResolver} and {@link NamespaceHandlerResolver} interfaces.
     * <p>
     * Simply delegates to the actual implementation discovered in a specific bundle.
     */
    private static class Plugin implements NamespaceHandlerResolver, EntityResolver
    {

        private final NamespaceHandlerResolver namespace;

        private final EntityResolver entity;
        private final ArtifactClassLoader artifactClassLoader;

        private Plugin(ArtifactClassLoader artifactClassLoader)
        {
            this.artifactClassLoader = artifactClassLoader;

            ClassLoader loader = artifactClassLoader.getClassLoader();

            //TODO(pablo.kraan): these resolvers are going to find all the resources available in the parent classlaoders too. Fix it
            entity = new DelegatingEntityResolver(loader);
            namespace = new DefaultNamespaceHandlerResolver(loader);
        }

        public NamespaceHandler resolve(String namespaceUri)
        {
            return namespace.resolve(namespaceUri);
        }

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
        {
            return entity.resolveEntity(publicId, systemId);
        }

        public ArtifactClassLoader getArtifactClassLoader()
        {
            return artifactClassLoader;
        }

    }

    private static final Log log = LogFactory.getLog(NamespacePlugins.class);

    //final LazyBundleRegistry.Condition condition = new LazyBundleRegistry.Condition() {
    //
    //    private final String NS_HANDLER_RESOLVER_CLASS_NAME = NamespaceHandlerResolver.class.getName();
    //
    //    public boolean pass(Bundle bundle) {
    //        try {
    //            Class<?> type = bundle.loadClass(NS_HANDLER_RESOLVER_CLASS_NAME);
    //            return NamespaceHandlerResolver.class.equals(type);
    //        } catch (Throwable th) {
    //            // if the interface is not wired, ignore the bundle
    //            log.warn("Bundle " + OsgiStringUtils.nullSafeNameAndSymName(bundle) + " cannot see class ["
    //                     + NS_HANDLER_RESOLVER_CLASS_NAME + "]; ignoring it as a namespace resolver");
    //
    //            return false;
    //        }
    //    }
    //};
    //
    //private final LazyBundleRegistry.Activator<Plugin> activation = new LazyBundleRegistry.Activator<Plugin>() {
    //
    //    public Plugin activate(Bundle bundle) {
    //        return new Plugin(bundle);
    //    }
    //};
    //
    //private final LazyBundleRegistry<Plugin> pluginRegistry =
    //        new LazyBundleRegistry<Plugin>(condition, activation, log);

    private final List<Plugin> pluginRegistry = new LinkedList<>();

    ///**
    // * Adds a bundle as a handler to plugin registry.
    // *
    // * @param bundle
    // * @param lazyBundle
    // */
    public void addPlugin(ArtifactClassLoader artifactClassLoader)
    {
        //boolean debug = log.isDebugEnabled();
        //
        //if (debug)
        //    log.debug("Adding as " + (lazyBundle ? "lazy " : "") + "namespace handler bundle "
        //              + OsgiStringUtils.nullSafeNameAndSymName(bundle));
        //
        pluginRegistry.add(new Plugin(artifactClassLoader));
    }
    //
    ///**
    // * Checks the type compatibility check between the namespace parser wired to Spring DM and the discovered bundle
    // * class space.
    // *
    // * @param bundle handler bundle
    // * @return true if there is type compatibility, false otherwise
    // */
    //boolean isTypeCompatible(Bundle bundle) {
    //    return condition.pass(bundle);
    //}
    //
    ///**
    // * Returns true if a handler mapping was removed for the given bundle.
    // *
    // * @param bundle bundle to look at
    // * @return true if the bundle was used in the plugin map
    // */
    //boolean removePlugin(Bundle bundle) {
    //    if (log.isDebugEnabled())
    //        log.debug("Removing handler " + OsgiStringUtils.nullSafeNameAndSymName(bundle));
    //
    //    return pluginRegistry.remove(bundle);
    //}

    public NamespaceHandler resolve(final String namespaceUri)
    {
        if (System.getSecurityManager() != null)
        {
            return AccessController.doPrivileged(new PrivilegedAction<NamespaceHandler>()
            {

                public NamespaceHandler run()
                {
                    return doResolve(namespaceUri);
                }
            });

        }
        else
        {
            return doResolve(namespaceUri);
        }
    }

    public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException
    {
        if (System.getSecurityManager() != null)
        {
            try
            {
                return AccessController.doPrivileged(new PrivilegedExceptionAction<InputSource>()
                {

                    public InputSource run() throws Exception
                    {
                        return doResolveEntity(publicId, systemId);
                    }
                });
            }
            catch (PrivilegedActionException pae)
            {
                Exception cause = pae.getException();
                handleInputSourceException(cause);
            }
        }
        else
        {
            try
            {
                return doResolveEntity(publicId, systemId);
            }
            catch (Exception ex)
            {
                handleInputSourceException(ex);
            }
        }

        return null;
    }

    private NamespaceHandler doResolve(final String namespaceUri)
    {
        final boolean debug = log.isDebugEnabled();
        final boolean trace = log.isTraceEnabled();

        if (debug)
        {
            log.debug("Trying to resolving namespace handler for " + namespaceUri);
        }

        for (Plugin plugin : pluginRegistry)
        {
            try
            {
                NamespaceHandler handler = plugin.resolve(namespaceUri);
                if (handler != null)
                {
                    //if (debug)
                    //{
                    //    log.debug("Namespace handler for " + namespaceUri + " found inside bundle "
                    //              + plugin.getArtifactClassLoader().getArtifactName());
                    //}
                    System.out.println("Namespace handler for " + namespaceUri + " found inside bundle "
                                       + plugin.getArtifactClassLoader().getArtifactName());

                    return handler;
                }
                else if (trace)
                {
                    //log.trace("Namespace handler for " + namespaceUri + " not found inside bundle "
                    //          + OsgiStringUtils.nullSafeNameAndSymName(plugin.getBundle()));
                    System.out.println("Namespace handler for " + namespaceUri + " not found inside bundle "
                                       + plugin.getArtifactClassLoader().getArtifactName());
                }
            }
            catch (IllegalArgumentException ex)
            {
                System.out.println("Namespace handler for " + namespaceUri + " not found inside bundle "
                                   + plugin.getArtifactClassLoader().getArtifactName());
            }
        }

        return null;
    }

    private InputSource doResolveEntity(final String publicId, final String systemId) throws Exception
    {
        final boolean debug = log.isDebugEnabled();
        final boolean trace = log.isTraceEnabled();

        if (debug)
        {
            log.debug("Trying to resolving entity for " + publicId + "|" + systemId);
        }

        //if (systemId != null) {
        //
        //    return pluginRegistry.apply(new LazyBundleRegistry.Operation<Plugin, InputSource>() {
        //
        //        public InputSource operate(Plugin plugin) throws SAXException, IOException {
        //            try {
        //                InputSource inputSource = plugin.resolveEntity(publicId, systemId);
        //
        //                if (inputSource != null) {
        //                    if (debug)
        //                        log.debug("XML schema for " + publicId + "|" + systemId + " found inside bundle "
        //                                  + OsgiStringUtils.nullSafeNameAndSymName(plugin.getBundle()));
        //                    return inputSource;
        //                }
        //
        //            } catch (FileNotFoundException ex) {
        //                if (trace)
        //                    log.trace("XML schema for " + publicId + "|" + systemId + " not found inside bundle "
        //                              + OsgiStringUtils.nullSafeNameAndSymName(plugin.getBundle()), ex);
        //            }
        //            return null;
        //        }
        //    });
        //}
        return null;
    }

    private void handleInputSourceException(Exception exception) throws SAXException, IOException
    {
        if (exception instanceof RuntimeException)
        {
            throw (RuntimeException) exception;
        }
        if (exception instanceof IOException)
        {
            throw (IOException) exception;
        }
        throw (SAXException) exception;
    }

    public void destroy()
    {
        //pluginRegistry.clear();
    }
}