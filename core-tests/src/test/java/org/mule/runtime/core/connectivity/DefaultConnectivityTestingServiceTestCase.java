/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.connectivity;

import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mule.runtime.api.connection.ConnectionValidationResult.success;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.connectivity.ConnectivityTestingObjectNotFoundException;
import org.mule.runtime.core.api.connectivity.ConnectivityTestingStrategy;
import org.mule.runtime.core.api.connectivity.UnsupportedConnectivityTestingObjectException;
import org.mule.runtime.core.api.lifecycle.InitialisationException;
import org.mule.runtime.core.api.registry.ServiceRegistry;
import org.mule.tck.junit4.AbstractMuleTestCase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DefaultConnectivityTestingServiceTestCase extends AbstractMuleTestCase {

  private static final String TEST_IDENTIFIER = "testIdentifier";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private MuleContext mockMuleContext = mock(MuleContext.class, RETURNS_DEEP_STUBS);
  private ServiceRegistry mockServiceRegistry = mock(ServiceRegistry.class, RETURNS_DEEP_STUBS);
  private ConnectivityTestingStrategy mockConnectivityTestingStrategy =
      mock(ConnectivityTestingStrategy.class, RETURNS_DEEP_STUBS);
  private DefaultConnectivityTestingService connectivityTestingService;
  private Object fakeConnectivityTestingObject = new Object();

  @Before
  public void createConnectivityService() throws InitialisationException {
    connectivityTestingService = new DefaultConnectivityTestingService();
    connectivityTestingService.setServiceRegistry(mockServiceRegistry);
    connectivityTestingService.setMuleContext(mockMuleContext);
    when(mockServiceRegistry.lookupProviders(any())).thenReturn(asList(mockConnectivityTestingStrategy));
    when(mockConnectivityTestingStrategy.accepts(fakeConnectivityTestingObject)).thenReturn(true);
    when(mockMuleContext.getRegistry().get(TEST_IDENTIFIER)).thenReturn(fakeConnectivityTestingObject);
    connectivityTestingService.initialise();
  }

  @Test
  public void testConnectionThrowsException() throws Exception {
    RuntimeException exception = new RuntimeException();
    when(mockConnectivityTestingStrategy.testConnectivity(fakeConnectivityTestingObject)).thenThrow(exception);
    ConnectionValidationResult validationResult = connectivityTestingService.testConnection(TEST_IDENTIFIER);
    assertThat(validationResult.isValid(), is(false));
    assertThat(validationResult.getException(), is(exception));
  }

  @Test
  public void testConnection() {
    when(mockConnectivityTestingStrategy.testConnectivity(fakeConnectivityTestingObject)).thenReturn(success());
    ConnectionValidationResult validationResult = connectivityTestingService.testConnection(TEST_IDENTIFIER);
    assertThat(validationResult.isValid(), is(true));
  }

  @Test
  public void testObjectNotSupported() {
    reset(mockConnectivityTestingStrategy);
    when(mockConnectivityTestingStrategy.accepts(fakeConnectivityTestingObject)).thenReturn(false);
    expectedException.expect(UnsupportedConnectivityTestingObjectException.class);
    connectivityTestingService.testConnection(TEST_IDENTIFIER);
  }

  @Test
  public void nonExistentConnectivityTestingObject() {
    reset(mockMuleContext);
    when(mockMuleContext.getRegistry().get(TEST_IDENTIFIER)).thenReturn(null);
    expectedException.expect(ConnectivityTestingObjectNotFoundException.class);
    connectivityTestingService.testConnection(TEST_IDENTIFIER);
  }

}
