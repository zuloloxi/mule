/**
 * (c) 2003-2015 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.ws.api.metadata.operation;

import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.List;

import javax.wsdl.BindingOperation;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;

/**
 * @author MuleSoft, Inc.
 */
public abstract class AbstractOperationIOResolver implements OperationIOResolver
{
    @Override
    public List<SOAPHeader> getHeaders(BindingOperation bindingOperation)
    {
        List<SOAPHeader> result = new ArrayList<SOAPHeader>();

        Optional<List> extensibilityElementsOptional = extensibilityElements(bindingOperation);
        if (extensibilityElementsOptional.isPresent()) {
            final List<?> extensibilityElements = extensibilityElementsOptional.get();
            for (Object element : extensibilityElements) {
                if (element != null && element instanceof SOAPHeader) {
                    result.add((SOAPHeader) element);
                }
            }
        }

        return result;
    }

    @Override
    public Optional<String> getBodyPartName(BindingOperation bindingOperation)
    {
        Optional<List> listOptional = extensibilityElements(bindingOperation);
        if (!listOptional.isPresent()){
            return Optional.absent();
        }
        for (Object object : listOptional.get())
        {
            if (object instanceof SOAPBody){ //TODO what about other type of SOAP body out there? (e.g.: SOAP12Body)
                SOAPBody soapBody = (SOAPBody) object;
                List soapBodyParts = soapBody.getParts();
                if (soapBodyParts.size()>1){
                    throw new RuntimeException("Warning: Operation Messages With More Than 1 Part Are Not Supported.");
                }
                if (soapBodyParts.isEmpty()){
                    return Optional.absent();
                }
                String partName = (String)soapBodyParts.get(0);
                return Optional.of(partName);
            }
        }

        return Optional.absent();
    }

    protected abstract Optional<List> extensibilityElements(BindingOperation bindingOperation);

}