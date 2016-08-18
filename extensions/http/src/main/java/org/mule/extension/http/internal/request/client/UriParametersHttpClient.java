/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request.client;

import org.mule.extension.http.api.request.client.UriParameters;
import org.mule.service.http.api.client.HttpClient;
import org.mule.service.http.api.client.HttpRequestAuthentication;
import org.mule.service.http.api.domain.request.HttpRequest;
import org.mule.service.http.api.domain.response.HttpResponse;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *
 */
public class UriParametersHttpClient implements org.mule.service.http.api.client.HttpClient {

  private HttpClient httpClient;
  private UriParameters uriParameters;

  public UriParametersHttpClient(HttpClient httpClient, UriParameters uriParameters) {
    this.httpClient = httpClient;
    this.uriParameters = uriParameters;
  }

  /**
   * Returns the default parameters for the {@link HttpRequest} URI.
   */
  public UriParameters getDefaultUriParameters() {
    return uriParameters;
  }

  @Override
  public void start() {
    httpClient.start();
  }

  @Override
  public void stop() {
    httpClient.stop();
  }

  @Override
  public HttpResponse send(HttpRequest request, int responseTimeout, boolean followRedirects,
                           HttpRequestAuthentication authentication)
      throws IOException, TimeoutException {
    return httpClient.send(request, responseTimeout, followRedirects, authentication);
  }
}
