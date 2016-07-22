/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api.security;

import static org.apache.ws.security.handler.WSHandlerConstants.SIGNATURE;
import static org.apache.ws.security.handler.WSHandlerConstants.SIG_PROP_REF_ID;
import static org.mule.extension.ws.api.security.util.WSCryptoUtils.createDefaultTrustStoreProperties;
import static org.mule.extension.ws.api.security.util.WSCryptoUtils.createTrustStoreProperties;
import org.mule.runtime.api.tls.TlsContextFactory;
import org.mule.runtime.extension.api.annotation.Parameter;

import java.util.Map;
import java.util.Properties;

/**
 * Verifies the signature of a SOAP response, using certificates of the trust-store in the provided TLS context.
 */
public class VerifySignatureSecurityStrategy extends AbstractSecurityStrategy
{

    private static final String WS_VERIFY_SIGNATURE_PROPERTIES_KEY = "verifySignatureProperties";

    @Parameter
    private TlsContextFactory tlsContextFactory;

    @Override
    public void apply(Map<String, Object> outConfigProperties, Map<String, Object> inConfigProperties)
    {
        appendAction(inConfigProperties, SIGNATURE);

        Properties signatureProperties;

        if (tlsContextFactory == null)
        {
            signatureProperties = createDefaultTrustStoreProperties();
        }
        else
        {
            signatureProperties = createTrustStoreProperties(tlsContextFactory.getTrustStoreConfiguration());
        }

        inConfigProperties.put(SIG_PROP_REF_ID, WS_VERIFY_SIGNATURE_PROPERTIES_KEY);
        inConfigProperties.put(WS_VERIFY_SIGNATURE_PROPERTIES_KEY, signatureProperties);
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
