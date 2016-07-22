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
import javax.wsdl.extensions.soap.SOAPHeader;

/**
 * @author MuleSoft, Inc.
 */
public interface OperationIOResolver
{
    Message getMessage(Operation operation);

    List<SOAPHeader> getHeaders(BindingOperation bindingOperation);

    Optional<String> getBodyPartName(BindingOperation bindingOperation);

}
