/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.http.internal.listener.grizzly;

import static java.lang.String.format;
import static org.mule.runtime.core.api.config.ThreadingProfile.DEFAULT_THREADING_PROFILE;
import static org.mule.runtime.core.util.concurrent.ThreadNameHelper.getPrefix;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.MuleException;
import org.mule.runtime.core.api.config.ThreadingProfile;
import org.mule.runtime.core.api.context.MuleContextAware;
import org.mule.runtime.core.api.context.WorkManager;
import org.mule.runtime.core.api.context.WorkManagerSource;
import org.mule.runtime.core.config.MutableThreadingProfile;
import org.mule.runtime.module.http.internal.listener.HttpListenerRegistry;
import org.mule.service.http.api.server.HttpServer;
import org.mule.service.http.api.server.PathAndMethodRequestMatcher;
import org.mule.service.http.api.server.RequestHandler;
import org.mule.service.http.api.server.RequestHandlerManager;
import org.mule.service.http.api.server.ServerAddress;

import java.io.IOException;

import org.glassfish.grizzly.nio.transport.TCPNIOServerConnection;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;

public class GrizzlyHttpServer implements HttpServer, WorkManagerSource, MuleContextAware {

  private static final int DEFAULT_MAX_THREADS = 128;

  private final TCPNIOTransport transport;
  private final ServerAddress serverAddress;
  private final HttpListenerRegistry listenerRegistry;
  private TCPNIOServerConnection serverConnection;
  private WorkManager workManager;
  private boolean stopped = true;
  private boolean stopping;
  private MuleContext muleContext;
  // TODO: MULE-9320 Define threading model for message sources in Mule 4 - This should be a parameter if nothing changes
  private ThreadingProfile workerThreadingProfile = new MutableThreadingProfile(DEFAULT_THREADING_PROFILE);

  private String ownerName;

  public GrizzlyHttpServer(ServerAddress serverAddress, TCPNIOTransport transport, HttpListenerRegistry listenerRegistry) {
    this.serverAddress = serverAddress;
    this.transport = transport;
    this.listenerRegistry = listenerRegistry;
  }

  @Override
  public synchronized void start() throws IOException {
    workerThreadingProfile.setMaxThreadsActive(DEFAULT_MAX_THREADS);
    workManager = createWorkManager(ownerName);
    try {
      workManager.start();
    } catch (MuleException e) {
      throw new IOException(e);
    }
    serverConnection = transport.bind(serverAddress.getIp(), serverAddress.getPort());
    stopped = false;
  }

  @Override
  public synchronized void stop() {
    stopping = true;
    try {
      transport.unbind(serverConnection);
      workManager.dispose();
    } finally {
      workManager = null;
      stopping = false;
    }
  }

  @Override
  public ServerAddress getServerAddress() {
    return serverAddress;
  }

  @Override
  public boolean isStopping() {
    return stopping;
  }

  @Override
  public boolean isStopped() {
    return stopped;
  }

  @Override
  public RequestHandlerManager addRequestHandler(PathAndMethodRequestMatcher requestMatcher, RequestHandler requestHandler) {
    return this.listenerRegistry.addRequestHandler(this, requestHandler, requestMatcher);
  }

  private WorkManager createWorkManager(String name) {
    final WorkManager workManager =
        workerThreadingProfile.createWorkManager(format("%s%s.%s", getPrefix(muleContext), name, "worker"),
                                                 muleContext.getConfiguration().getShutdownTimeout());
    if (workManager instanceof MuleContextAware) {
      ((MuleContextAware) workManager).setMuleContext(muleContext);
    }
    return workManager;
  }

  @Override
  public void setMuleContext(MuleContext context) {
    this.muleContext = context;
  }

  @Override
  public WorkManager getWorkManager() throws MuleException {
    return workManager;
  }
}
