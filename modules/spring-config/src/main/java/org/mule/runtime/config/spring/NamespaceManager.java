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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class NamespaceManager  implements InitializingBean, DisposableBean
{

    private static final Log log = LogFactory.getLog(NamespaceManager.class);
    private final ArtifactClassLoader artifactClassLoader;

    /** The set of all namespace plugins known to the extender */
    //private NamespacePlugins namespacePlugins;

    /**
     * ServiceRegistration object returned by OSGi when registering the NamespacePlugins instance as a service
     */
    //private ServiceRegistration nsResolverRegistration, enResolverRegistration = null;

    /** OSGi Environment */
    //private final BundleContext context;

    private final String extenderInfo;

    private static final String META_INF = "META-INF/";

    private static final String SPRING_HANDLERS = "spring.handlers";

    private static final String SPRING_SCHEMAS = "spring.schemas";

   public NamespaceManager(ArtifactClassLoader artifactClassLoader) {
        this.artifactClassLoader = artifactClassLoader;
        //this.context = context;
        extenderInfo = artifactClassLoader.getArtifactName();

        maybeAddNamespaceHandlerFor(artifactClassLoader);

        // detect package admin
        //this.namespacePlugins = new NamespacePlugins();
    }

    //TODO(pablo.kraan): fix all javadocs
    /**
     * Registers the namespace plugin handler if this bundle defines handler mapping or schema mapping resources.
     *
     * <p/> This method considers only the bundle space and not the class space.
     */
    public void maybeAddNamespaceHandlerFor(ArtifactClassLoader artifactClassLoader) {
        boolean debug = log.isDebugEnabled();
        boolean trace = log.isTraceEnabled();
        boolean hasHandlers;
        boolean hasSchemas;

        if (trace) {
            log.trace("Inspecting bundle " + artifactClassLoader + " for Spring namespaces");
        }
        try
        {
            hasHandlers = findResources(artifactClassLoader);

            //TODO(pablo.kraan): use findResource here too
            //TODO(pablo.kraan): there is no need to check for this if there are handlers already found
            hasSchemas = artifactClassLoader.getClassLoader().getResources(META_INF + SPRING_SCHEMAS).hasMoreElements();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        // if the bundle defines handlers
        if (hasHandlers) {

            //if (trace)
            //    log.trace("Bundle " + bundle + " provides Spring namespace handlers...");

            //if (isLazyBundle) {
            //    this.namespacePlugins.addPlugin(bundle, isLazyBundle, true);
            //} else {
                // check type compatibility between the bundle's and spring-extender's spring version
                //if (hasCompatibleNamespaceType(bundle)) {
                    NamespacePlugins.instance.addPlugin(artifactClassLoader);
                //} else {
                //    if (debug)
                //        log.debug("Bundle [" + OsgiStringUtils.nullSafeNameAndSymName(bundle)
                //                  + "] declares namespace handlers but is not compatible with extender [" + extenderInfo
                //                  + "]; ignoring...");
                //}
            //}
        } else {
            // bundle declares only schemas, add it though the handlers might not be compatible...
            if (hasSchemas) {
                NamespacePlugins.instance.addPlugin(artifactClassLoader);
                //if (trace)
                //    log.trace("Bundle " + bundle + " provides Spring schemas...");
            }
        }
    }

    private boolean findResources(ArtifactClassLoader artifactClassLoader) throws IOException
    {
        boolean hasHandlers;
        final Enumeration<URL> resources = artifactClassLoader.findResources(META_INF + SPRING_HANDLERS);
        hasHandlers = resources.hasMoreElements();
        if (hasHandlers)
        {
            String schemas = "Found schemas with findResource: ";
            while (resources.hasMoreElements())
            {
                final URL url = resources.nextElement();
                schemas += "\n" + url;
            }
            System.out.println(schemas);
        }
        return hasHandlers;
    }

    //private boolean hasCompatibleNamespaceType(Bundle bundle) {
    //    return namespacePlugins.isTypeCompatible(bundle);
    //}

    ///**
    // * Removes the target bundle from the set of those known to provide handler or schema mappings.
    // *
    // * @param bundle handler bundle
    // */
    //public void maybeRemoveNameSpaceHandlerFor(Bundle bundle) {
    //    Assert.notNull(bundle);
    //    boolean removed = this.namespacePlugins.removePlugin(bundle);
    //    if (removed && log.isDebugEnabled()) {
    //        log.debug("Removed namespace handler resolver for " + OsgiStringUtils.nullSafeNameAndSymName(bundle));
    //    }
    //}

    ///**
    // * Registers the NamespacePlugins instance as an Osgi Resolver service
    // */
    //private void registerResolverServices() {
    //    if (log.isDebugEnabled()) {
    //        log.debug("Registering Spring NamespaceHandlerResolver and EntityResolver...");
    //    }
    //
    //    Bundle bnd = BundleUtils.getDMCoreBundle(context);
    //    Dictionary<String, Object> props = null;
    //    if (bnd != null) {
    //        props = new Hashtable<String, Object>();
    //        props.put(BundleUtils.DM_CORE_ID, bnd.getBundleId());
    //        props.put(BundleUtils.DM_CORE_TS, bnd.getLastModified());
    //    }
    //    nsResolverRegistration =
    //            context.registerService(new String[] { NamespaceHandlerResolver.class.getName() },
    //                                    this.namespacePlugins, props);
    //
    //    enResolverRegistration =
    //            context.registerService(new String[] { EntityResolver.class.getName() }, this.namespacePlugins, props);
    //
    //}

    ///**
    // * Unregisters the NamespaceHandler and EntityResolver service
    // */
    //private void unregisterResolverService() {
    //
    //    boolean result = OsgiServiceUtils.unregisterService(nsResolverRegistration);
    //    result = result || OsgiServiceUtils.unregisterService(enResolverRegistration);
    //
    //    if (result) {
    //        if (log.isDebugEnabled())
    //            log.debug("Unregistering Spring NamespaceHandler and EntityResolver service");
    //    }
    //
    //    this.nsResolverRegistration = null;
    //    this.enResolverRegistration = null;
    //}

    //public NamespacePlugins getNamespacePlugins() {
    //    return namespacePlugins;
    //}

    //
    // Lifecycle methods
    //

    @Override
    public void afterPropertiesSet() {
    //    registerResolverServices();
    }
    //
    @Override
    public void destroy() {
    //    unregisterResolverService();
    //    this.namespacePlugins.destroy();
    //    this.namespacePlugins = null;
    }
}