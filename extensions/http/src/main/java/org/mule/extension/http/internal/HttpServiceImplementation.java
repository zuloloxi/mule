/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal;

import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.initialiseIfNeeded;
import org.mule.extension.http.api.server.HttpListenerConnectionManager;
import org.mule.extension.http.internal.request.grizzly.GrizzlyHttpClient;
import org.mule.runtime.core.api.lifecycle.Initialisable;
import org.mule.runtime.core.api.lifecycle.InitialisationException;
import org.mule.service.http.api.HttpService;
import org.mule.service.http.api.client.HttpClientFactory;
import org.mule.service.http.api.server.HttpServerFactory;

import javax.inject.Inject;

public class HttpServiceImplementation implements HttpService, Initialisable {

  private static final String OBJECT_HTTP_CLIENT_FACTORY = "_httpClientFactory";

  @Inject
  private HttpListenerConnectionManager connectionManager;

  @Override
  public HttpServerFactory getServerFactory() {
    return connectionManager;
  }

  @Override
  public HttpClientFactory getClientFactory() {
    //HttpClientFactory httpClientFactory = muleContext.getRegistry().get(OBJECT_HTTP_CLIENT_FACTORY);
    //if (httpClientFactory == null) {
    return GrizzlyHttpClient::new;
    //} else {
    //  return httpClientFactory;
    //}
  }

  @Override
  public String getName() {
    return "HTTP Service";
  }

  @Override
  public void initialise() throws InitialisationException {
    initialiseIfNeeded(connectionManager);
  }
}
