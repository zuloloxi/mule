/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api.metadata;

import static java.util.stream.Collectors.toSet;
import static org.mule.runtime.api.metadata.MetadataKeyBuilder.newKey;
import org.mule.extension.ws.api.WSConsumerConnection;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataKey;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.metadata.resolving.MetadataKeysResolver;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.wsdl.Operation;

public class WSOperationKeyMetadataResolver implements MetadataKeysResolver
{

    @Override
    public Set<MetadataKey> getMetadataKeys(MetadataContext context) throws MetadataResolvingException, ConnectionException
    {
            Optional<WSConsumerConnection> connection = context.getConnection();
            if (!connection.isPresent())
            {
               throw new ConnectionException("No WebService Consumer connection was found to fetch the metadata keys");
            }

            List<Operation> operations = connection.get().getBinding().getPortType().getOperations();

            return operations.stream()
                    .map(ope -> newKey(ope.getName()).build())
                    .collect(toSet());
    }
}
