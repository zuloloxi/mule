/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws.internal.exception;

import static java.lang.String.format;

public class WSConsumerException extends RuntimeException
{

    public WSConsumerException(String message, Object... messageElements)
    {
        super(format(message, messageElements));
    }

    public WSConsumerException(String message, Throwable cause, Object... messageElements)
    {
        super(format(message, messageElements), cause);
    }

    public WSConsumerException(String message, Throwable cause)
    {
        super(message, cause);

    }
    public WSConsumerException(Throwable cause)
    {
        super(cause);
    }
}
