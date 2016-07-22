/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api.security;

import static org.apache.ws.security.handler.WSHandlerConstants.ENCRYPT;
import static org.apache.ws.security.handler.WSHandlerConstants.ENCRYPTION_USER;
import static org.apache.ws.security.handler.WSHandlerConstants.ENC_PROP_REF_ID;
import static org.mule.extension.ws.api.security.util.WSCryptoUtils.createDefaultTrustStoreProperties;
import static org.mule.extension.ws.api.security.util.WSCryptoUtils.createTrustStoreProperties;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.extension.api.annotation.Parameter;

import java.util.Map;
import java.util.Properties;

/**
 * Verifies the signature of a SOAP response, using certificates of the trust-store in the provided TLS context.
 */
public class EncryptSecurityStrategy extends AbstractSecurityStrategy
{

    private static final String WS_ENCRYPT_PROPERTIES_KEY = "encryptProperties";

    @Parameter
    private TlsContextFactory tlsContextFactory;

    @Parameter
    private String alias;

    @Override
    public void apply(Map<String, Object> outConfigProperties, Map<String, Object> inConfigProperties)
    {
        appendAction(outConfigProperties, ENCRYPT);

        Properties encryptionProperties;

        if (tlsContextFactory == null)
        {
            encryptionProperties = createDefaultTrustStoreProperties();
        }
        else
        {
            encryptionProperties = createTrustStoreProperties(tlsContextFactory.getTrustStoreConfiguration());
        }

        outConfigProperties.put(ENC_PROP_REF_ID, WS_ENCRYPT_PROPERTIES_KEY);
        outConfigProperties.put(WS_ENCRYPT_PROPERTIES_KEY, encryptionProperties);
        outConfigProperties.put(ENCRYPTION_USER, alias);
    }


    public TlsContextFactory getTlsContext()
    {
        return tlsContextFactory;
    }

    public void setTlsContext(TlsContextFactory tlsContextFactory)
    {
        this.tlsContextFactory = tlsContextFactory;
    }

    public String getAlias()
    {
        return alias;
    }

    public void setAlias(String alias)
    {
        this.alias = alias;
    }
}
