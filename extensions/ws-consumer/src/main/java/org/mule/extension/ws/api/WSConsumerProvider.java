/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api;

import org.mule.extension.ws.api.security.SecurityStrategy;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionHandlingStrategy;
import org.mule.runtime.api.connection.ConnectionHandlingStrategyFactory;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.module.http.api.requester.HttpRequesterConfig;

import java.util.List;

@Alias("ws")
public class WSConsumerProvider implements ConnectionProvider<WSConsumerConnection>
{
    @Parameter
    private String wsdlLocation;

    @Parameter
    private String service;

    @Parameter
    private String port;

    @Parameter
    private String serviceAddress;

    @Parameter
    @Optional
    private HttpRequesterConfig connectorConfig;

    @Parameter
    @Optional
    private List<SecurityStrategy> securities;

    @Override
    public WSConsumerConnection connect() throws ConnectionException
    {
        return new WSConsumerConnection(wsdlLocation, service, port, securities, serviceAddress, connectorConfig);
    }

    @Override
    public void disconnect(WSConsumerConnection connection)
    {
        // nothing
    }

    @Override
    public ConnectionValidationResult validate(WSConsumerConnection connection)
    {
        return connection.isValid();
    }

    @Override
    public ConnectionHandlingStrategy<WSConsumerConnection> getHandlingStrategy(ConnectionHandlingStrategyFactory<WSConsumerConnection> connectionHandlingStrategyFactory)
    {
        return connectionHandlingStrategyFactory.supportsPooling();
    }
}
