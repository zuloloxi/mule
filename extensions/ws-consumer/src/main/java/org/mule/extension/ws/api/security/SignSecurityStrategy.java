/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api.security;

import static org.apache.ws.security.handler.WSHandlerConstants.SIGNATURE;
import static org.apache.ws.security.handler.WSHandlerConstants.SIGNATURE_USER;
import static org.apache.ws.security.handler.WSHandlerConstants.SIG_PROP_REF_ID;
import static org.mule.extension.ws.api.security.util.WSCryptoUtils.createKeyStoreProperties;
import org.mule.extension.ws.api.security.callback.WSPasswordCallbackHandler;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.api.tls.TlsContextKeyStoreConfiguration;
import org.mule.runtime.extension.api.annotation.Parameter;

import java.util.Map;
import java.util.Properties;

import org.apache.ws.security.WSPasswordCallback;

/**
 * Signs the SOAP request that is being sent, using the private key of the key-store in the provided TLS context.
 */
public class SignSecurityStrategy extends AbstractSecurityStrategy
{

    private static final String WS_SIGN_PROPERTIES_KEY = "signProperties";

    @Parameter
    private TlsContextFactory tlsContextFactory;

    @Override
    public void apply(Map<String, Object> outConfigProperties, Map<String, Object> inConfigProperties)
    {
        final TlsContextKeyStoreConfiguration keyStoreConfig = tlsContextFactory.getKeyStoreConfiguration();

        appendAction(outConfigProperties, SIGNATURE);

        Properties signProperties = createKeyStoreProperties(keyStoreConfig);

        outConfigProperties.put(SIG_PROP_REF_ID, WS_SIGN_PROPERTIES_KEY);
        outConfigProperties.put(WS_SIGN_PROPERTIES_KEY, signProperties);
        outConfigProperties.put(SIGNATURE_USER, keyStoreConfig.getAlias());

        addPasswordCallbackHandler(outConfigProperties, new WSPasswordCallbackHandler(WSPasswordCallback.SIGNATURE)
        {
            @Override
            public void handle(WSPasswordCallback passwordCallback)
            {
                passwordCallback.setPassword(keyStoreConfig.getKeyPassword());
            }
        });
    }

    public TlsContextFactory getTlsContext()
    {
        return tlsContextFactory;
    }

    public void setTlsContext(TlsContextFactory tlsContextFactory)
    {
        this.tlsContextFactory = tlsContextFactory;
    }

}
