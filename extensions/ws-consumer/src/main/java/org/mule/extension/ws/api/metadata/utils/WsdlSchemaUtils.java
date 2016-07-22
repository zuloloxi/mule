/**
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.ws.api.metadata.utils;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Types;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class WsdlSchemaUtils {

    @SuppressWarnings("unchecked")
    public static List<String> getSchemas(Definition wsdlDefinition)
    {
        final Map<String, String> wsdlNamespaces = wsdlDefinition.getNamespaces();
        final List<String> schemas = new ArrayList<>();
        try
        {
            final List<Types> typesList = new ArrayList<>();
            extractWsdlTypes(wsdlDefinition, typesList);
            for (Types types : typesList)
            {
                for (Object o : types.getExtensibilityElements())
                {
                    if (o instanceof Schema)
                    {
                        schemas.addAll(resolveSchema(wsdlNamespaces, (Schema) o));
                    }
                }
            }

            // Allow importing types from other wsdl
            for (Object wsdlImportList : wsdlDefinition.getImports().values())
            {
                final List<Import> importList = (List<Import>) wsdlImportList;
                for (Import wsdlImport : importList)
                {
                    schemas.addAll(getSchemas(wsdlImport.getDefinition()));
                }
            }
        } catch (TransformerException e)
        {
            throw new IllegalStateException("There was an issue while obtaining schemas.", e);
        }

        return schemas;
    }

    public static List<String> resolveSchema(final Map<String, String> wsdlNamespaces, Schema schema) throws TransformerException
    {
        final List<String> schemas = new ArrayList<String>();
        fixPrefix(wsdlNamespaces, schema);
        fixSchemaLocations(schema);
        String flatSchema = schemaToString(schema);
        schemas.add(flatSchema);
        // STUDIO-5814: generates an issue adding duplicated schemas
        // for (Object location : schema.getIncludes()) {
        // schemas.add(schemaToString(((SchemaReference) location).getReferencedSchema()));
        // }
        return schemas;
    }

    /**
     * Extracts the "Types" definition from a WSDL and recursively from all the imports. The types are added to the typesList argument.
     */
    private static void extractWsdlTypes(Definition wsdlDefinition, List<Types> typesList) {
        // Add current types definition if present
        if (wsdlDefinition.getTypes() != null) {
            typesList.add(wsdlDefinition.getTypes());
        }
    }

    private static void fixPrefix(Map<String, String> wsdlNamespaces, Schema schema) {
        for (Map.Entry<String, String> entry : wsdlNamespaces.entrySet()) {
            boolean isDefault = StringUtils.isEmpty(entry.getKey());
            boolean containNamespace = schema.getElement().hasAttribute("xmlns:" + entry.getKey());
            if (!isDefault && !containNamespace) {
                schema.getElement().setAttribute("xmlns:" + entry.getKey(), entry.getValue());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void fixSchemaLocations(Schema schema) {
        // fix imports schemaLocation in pojo
        final String basePath = getBasePath(schema.getDocumentBaseURI());
        Map<String, Vector<SchemaImport>> oldImports = schema.getImports();
        Collection<Vector<SchemaImport>> values = oldImports.values();
        if (!values.isEmpty()) {
            for (Vector<SchemaImport> schemaImports : values) {
                for (SchemaImport schemaImport : schemaImports) {
                    String schemaLocationURI = schemaImport.getSchemaLocationURI();
                    if (schemaLocationURI != null && !schemaLocationURI.startsWith(basePath) && !schemaLocationURI.startsWith("http")) {
                        schemaImport.setSchemaLocationURI(basePath + schemaLocationURI);
                    }
                }
            }

            // fix imports schemaLocation in dom
            NodeList children = schema.getElement().getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node item = children.item(i);
                if ("import".equals(item.getLocalName())) {
                    NamedNodeMap attributes = item.getAttributes();
                    Node namedItem = attributes.getNamedItem("schemaLocation");
                    if (namedItem != null) {
                        String schemaLocation = namedItem.getNodeValue();
                        if (!schemaLocation.startsWith(basePath) && !schemaLocation.startsWith("http")) {
                            namedItem.setNodeValue(basePath + schemaLocation);
                        }
                    }
                }
            }
        }
    }

    private static String getBasePath(final String documentURI) {
        File document = new File(documentURI);
        if (document.isDirectory()) {
            return documentURI;
        }

        String fileName = document.getName();
        int fileNameIndex = documentURI.lastIndexOf(fileName);
        if (fileNameIndex == -1) {
            return documentURI;
        }

        return documentURI.substring(0, fileNameIndex);
    }

    private static String schemaToString(final Schema schema) throws TransformerException
    {
        Element element = schema.getElement();
        String result = elementToString(element);
        return result;
    }

    private static String elementToString(Element element) throws TransformerException
    {
        StringWriter writer = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(element), new StreamResult(writer));
        return writer.toString();
    }
}
