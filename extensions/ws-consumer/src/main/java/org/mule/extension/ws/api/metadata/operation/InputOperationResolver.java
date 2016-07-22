/**
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.ws.api.metadata.operation;

import com.google.common.base.Optional;

import java.util.List;

import javax.wsdl.BindingOperation;
import javax.wsdl.Message;
import javax.wsdl.Operation;

/**
 * @author MuleSoft, Inc.
 */
public class InputOperationResolver extends AbstractOperationIOResolver
{
    @Override
    public Message getMessage(Operation operation)
    {
        return operation.getInput().getMessage();
    }

    @Override
    protected Optional<List> extensibilityElements(BindingOperation bindingOperation)
    {
        if (bindingOperation == null) {
            return Optional.absent();
        }
        return Optional.fromNullable(bindingOperation.getBindingInput().getExtensibilityElements());
    }
}

