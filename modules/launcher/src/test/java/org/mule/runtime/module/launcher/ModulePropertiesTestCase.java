/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.runtime.module.launcher;

import static org.mule.runtime.module.artifact.classloader.ArtifactClassLoaderFilter.EXPORTED_CLASS_PACKAGES_PROPERTY;
import static org.mule.runtime.module.artifact.classloader.ArtifactClassLoaderFilter.EXPORTED_RESOURCE_PACKAGES_PROPERTY;

import org.mule.runtime.core.util.FileUtils;
import org.mule.runtime.core.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ModulePropertiesTestCase
{


    @Test
    public void testGeneration() throws IOException
    {
        //generateMuleModuleProperties("core", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/");
        //generateMuleModuleProperties("artifact", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/modules/");
        //generateMuleModuleProperties("spring-config", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/modules/");
        //generateMuleModuleProperties("extensions-spring-support", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/modules/");
        //generateMuleModuleProperties("extensions-support", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/modules/");
        //generateMuleModuleProperties("http", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/modules/");
        //generateMuleModuleProperties("cxf", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/modules/");
        //generateMuleModuleProperties("scripting", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/modules/");
        //generateMuleModuleProperties("xml", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/modules/");
        //generateMuleModuleProperties("management", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/modules/");
        //generateMuleModuleProperties("tls", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/modules/");
        //generateMuleModuleProperties("functional", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/tests/");
        //generateMuleModuleProperties("unit", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/tests/");
        //generateMuleModuleProperties("spring-security", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/modules/");
        //generateMuleModuleProperties("sockets", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/modules/");
        //generateMuleModuleProperties("spring-extras", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/modules/");
        //generateMuleModuleProperties("jboss-transactions", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/modules/");
        //generateMuleModuleProperties("db", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/modules/");
        //generateMuleModuleProperties("metadata-extension", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/tests/test-extensions/");
        //generateMuleModuleProperties("mule-api", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/");
        //generateMuleModuleProperties("mule-extensions-api", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule-extensions-api/");
        //generateMuleModuleProperties("file", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/extensions/");
        //generateMuleModuleProperties("file-extension-common", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/modules/");
        //generateMuleModuleProperties("jaas", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/modules/");
        //generateMuleModuleProperties("validation", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/extensions/");
        //generateMuleModuleProperties("json", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/modules/");
        //generateMuleModuleProperties("oauth", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/modules/");
        //generateMuleModuleProperties("pgp", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/modules/");
        //generateMuleModuleProperties("schedulers", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/modules/");
        //generateMuleModuleProperties("ws", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/modules/");
        //generateMuleModuleProperties("ftp", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/extensions/");
        //generateMuleModuleProperties("module-support", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/transports/");
        //generateMuleModuleProperties("core", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/transports/");
        //generateMuleModuleProperties("vm", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/transports/");
        generateMuleModuleProperties("jms", "/Users/pablokraan/devel/workspaces/muleFull-4.x4/mule/transports/");
    }

    private void generateMuleModuleProperties(String moduleName, String moduleBasePath) throws IOException
    {
        String modulePath = moduleBasePath + moduleName;
        if (!new File(modulePath).exists())
        {
            throw new IllegalArgumentException("Folder "+ modulePath +  " does not exists");
        }

        final File javaCodePath = new File(modulePath + "/src/main/java");
        final File resourcesPath = new File(modulePath + "/src/main/resources");

        final List<String> javaPackages = getJavaPackages(javaCodePath);
        final List<String> resourcePackages = getResourcePackages(resourcesPath);

        System.out.println("\nPackages:");
        for (String javaPackage : javaPackages)
        {
            System.out.println(javaPackage);
        }

        System.out.println("\nResources:");
        for (String resourcePackage : resourcePackages)
        {
            System.out.println(resourcePackage);
        }


        StringBuilder builder = new StringBuilder();
        builder.append("module.name").append("=").append(moduleName).append("\n");
        builder.append("\n");

        if (!javaPackages.isEmpty())
        {
            builder.append(EXPORTED_CLASS_PACKAGES_PROPERTY).append("=");
            final String tab = StringUtils.repeat(" ", EXPORTED_CLASS_PACKAGES_PROPERTY.length());
            boolean firstElement = true;
            for (String javaPackage : javaPackages)
            {
                if (firstElement)
                {
                    firstElement = false;
                }
                else
                {
                    builder.append(",\\\n").append(tab);
                }
                builder.append(javaPackage);

            }
        }

        builder.append("\n\n");

        if (!resourcePackages.isEmpty())
        {
            builder.append(EXPORTED_RESOURCE_PACKAGES_PROPERTY).append("=");
            final String tab = StringUtils.repeat(" ", EXPORTED_RESOURCE_PACKAGES_PROPERTY.length());
            boolean firstElement = true;
            for (String resourcePackage : resourcePackages)
            {
                if (firstElement)
                {
                    firstElement = false;
                }
                else
                {
                    builder.append(",\\\n").append(tab);
                }
                builder.append(resourcePackage);
            }
        }

        FileUtils.stringToFile("/tmp/mule-module.properties", builder.toString());
    }

    private List<String> getJavaPackages(File folder)
    {
        List<String> files = new java.util.LinkedList<>();
        listNonEmptyFolders(files, folder);

        List<String> packages = new ArrayList<>(files.size());

        for (String file : files)
        {
            final int length = folder.getAbsolutePath().length() + 1;
            final String packageName = file.substring(length).replace("/", ".");
            packages.add(packageName);
        }

        return packages;
    }

    private List<String> getResourcePackages(File folder)
    {
        List<String> files = new java.util.LinkedList<>();
        listNonEmptyFolders(files, folder);

        List<String> packages = new ArrayList<>(files.size());

        for (String file : files)
        {
            final int length = folder.getAbsolutePath().length();
            String packageName = file.substring(length);
            if (packageName.equals(""))
            {
                packageName = "/";
            }
            packages.add(packageName);
        }

        return packages;
    }

    private static void listNonEmptyFolders(List<String> files, File directory)
    {
        File[] found = directory.listFiles();

        boolean includeDirectory = false;

        if (found != null)
        {
            int position = files.size();
            for (File file : found)
            {
                if (file.isDirectory())
                {
                    listNonEmptyFolders(files, file);
                }
                else
                {
                    includeDirectory = true;
                }
            }

            if (includeDirectory)
            {
                files.add(position, directory.getAbsolutePath());
            }
        }
    }
}
