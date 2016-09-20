/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.processor.chain;

import static java.util.Arrays.asList;
import static org.mule.runtime.core.execution.MessageProcessorExecutionTemplate.createExecutionTemplate;
import static org.mule.runtime.core.message.DefaultEventBuilder.EventImplementation.setCurrentEvent;

import org.mule.runtime.core.MessageExchangePattern;
import org.mule.runtime.core.VoidMuleEvent;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.MuleException;
import org.mule.runtime.core.api.component.Component;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.processor.MessageProcessorChain;
import org.mule.runtime.core.api.transformer.Transformer;
import org.mule.runtime.core.api.transport.LegacyOutboundEndpoint;
import org.mule.runtime.core.execution.MessageProcessorExecutionTemplate;
import org.mule.runtime.core.message.DefaultEventBuilder;
import org.mule.runtime.core.routing.MessageFilter;

import java.util.ArrayList;
import java.util.List;

public class DefaultMessageProcessorChain extends AbstractMessageProcessorChain {

  protected MessageProcessorExecutionTemplate messageProcessorExecutionTemplate = createExecutionTemplate();

  protected DefaultMessageProcessorChain(List<Processor> processors) {
    super(null, processors);
  }

  protected DefaultMessageProcessorChain(Processor... processors) {
    super(null, new ArrayList<>(asList(processors)));
  }

  protected DefaultMessageProcessorChain(String name, List<Processor> processors) {
    super(name, processors);
  }

  protected DefaultMessageProcessorChain(String name, Processor... processors) {
    super(name, new ArrayList<>(asList(processors)));
  }

  public static DefaultMessageProcessorChain from(MuleContext muleContext, Processor messageProcessor) {
    return new DefaultMessageProcessorChain(messageProcessor);
  }

  public static MessageProcessorChain from(MuleContext muleContext, Processor... messageProcessors) throws MuleException {
    return new DefaultMessageProcessorChainBuilder(muleContext).chain(messageProcessors).build();
  }

  public static MessageProcessorChain from(MuleContext muleContext, List<Processor> messageProcessors)
      throws MuleException {
    return new DefaultMessageProcessorChainBuilder(muleContext).chain(messageProcessors).build();
  }

  protected Event doProcess(Event event) throws MuleException {
    Event copy;

    for (int i = 0; i < processors.size(); i++) {
      Processor processor = processors.get(i);
      copy = event;

      event = messageProcessorExecutionTemplate.execute(processor, event);

      if (VoidMuleEvent.getInstance().equals(event)) {
        setCurrentEvent(copy);
        event = copy;
      } else if (event == null) {
        return null;
      }
    }
    return event;
  }

  public void setTemplateMuleContext(MuleContext context) {
    messageProcessorExecutionTemplate.setMuleContext(context);
  }

  @Override
  public void setFlowConstruct(FlowConstruct flowConstruct) {
    super.setFlowConstruct(flowConstruct);
    messageProcessorExecutionTemplate.setFlowConstruct(flowConstruct);
  }
}
