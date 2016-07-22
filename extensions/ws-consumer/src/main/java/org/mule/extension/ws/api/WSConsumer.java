/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.api;

import org.mule.extension.ws.api.security.DecryptSecurityStrategy;
import org.mule.extension.ws.api.security.EncryptSecurityStrategy;
import org.mule.extension.ws.api.security.SecurityStrategy;
import org.mule.extension.ws.api.security.SignSecurityStrategy;
import org.mule.extension.ws.api.security.TimestampSecurityStrategy;
import org.mule.extension.ws.api.security.UsernameTokenSecurityStrategy;
import org.mule.extension.ws.api.security.VerifySignatureSecurityStrategy;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.Parameter;
import org.mule.runtime.extension.api.annotation.SubTypeMapping;
import org.mule.runtime.extension.api.annotation.capability.Xml;
import org.mule.runtime.extension.api.annotation.connector.Providers;
import org.mule.runtime.extension.api.annotation.param.Optional;

@Operations(ConsumeOperation.class)
@Providers({WSConsumerProvider.class})
@Extension(name = "Web Service Consumer Extension")
@SubTypeMapping(baseType = SecurityStrategy.class,
        subTypes = {
                EncryptSecurityStrategy.class,
                DecryptSecurityStrategy.class,
                SignSecurityStrategy.class,
                TimestampSecurityStrategy.class,
                UsernameTokenSecurityStrategy.class,
                VerifySignatureSecurityStrategy.class
        })
@Xml(namespace = "wsc")
public class WSConsumer
{
    public static final String SOAP_HEADERS_PROPERTY_PREFIX = "soap.";

    @Parameter
    @Optional(defaultValue = "false")
    private boolean mtomEnabled;

    public boolean isMtomEnabled()
    {
        return mtomEnabled;
    }
}
