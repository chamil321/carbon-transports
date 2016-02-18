/*
 *  Copyright (c) 2015 WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.transport.http.netty.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.CarbonTransportInitializer;
import org.wso2.carbon.messaging.MessagingHandler;
import org.wso2.carbon.transport.http.netty.internal.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.internal.config.Parameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Declarative service component for the Netty transport. This handles registration &amp; unregistration of relevant
 * OSGi services.
 */
@Component(
        name = "org.wso2.carbon.transport.http.netty.internal.NettyTransportServiceComponent",
        immediate = true,
        property = {
                "capability-name=org.wso2.carbon.messaging.CarbonTransportInitializer, "
                        + "org.wso2.carbon.messaging.CarbonMessageProcessor",
                "component-key=netty-transports-mgt"
        }
)
@SuppressWarnings("unused")
public class NettyTransportServiceComponent implements RequiredCapabilityListener {

    private static final Logger log = LoggerFactory.getLogger(NettyTransportServiceComponent.class);

    private static final String CHANNEL_ID_KEY = "channel.id";

    private NettyTransportContextHolder dataHolder = NettyTransportContextHolder.getInstance();

    @Activate
    protected void activate(BundleContext bundleContext) {
        // Nothing to do
    }

    @Reference(
            name = "transport-initializer",
            service = CarbonTransportInitializer.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeTransportInitializer"
    )
    protected void addTransportInitializer(CarbonTransportInitializer serverInitializer, Map<String, ?> ref) {
        ListenerConfiguration listenerConfiguration = NettyTransportContextHolder.getInstance()
                .getListenerConfiguration((String) ref.get(CHANNEL_ID_KEY));

        List<Parameter> parameters = listenerConfiguration.getParameters();
        if (parameters != null && !parameters.isEmpty()) {
            Map<String, String> paramMap = new HashMap<>(parameters.size());
            for (Parameter parameter : parameters) {
                paramMap.put(parameter.getName(), parameter.getValue());
            }
            serverInitializer.setup(paramMap);
        }
        NettyTransportContextHolder.getInstance()
                .addNettyChannelInitializer((String) ref.get(CHANNEL_ID_KEY), serverInitializer);
    }

    protected void removeTransportInitializer(CarbonTransportInitializer serverInitializer, Map<String, ?> ref) {
        NettyTransportContextHolder.getInstance().removeNettyChannelInitializer((String) ref.get(CHANNEL_ID_KEY));
    }

    @Reference(
            name = "message-processor",
            service = CarbonMessageProcessor.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeMessageProcessor"
    )
    protected void addMessageProcessor(CarbonMessageProcessor carbonMessageProcessor) {
        NettyTransportContextHolder.getInstance().addMessageProcessor(carbonMessageProcessor);
    }

    protected void removeMessageProcessor(CarbonMessageProcessor carbonMessageProcessor) {
        NettyTransportContextHolder.getInstance().removeMessageProcessor(carbonMessageProcessor);
    }

    @Reference(
            name = "netty-stat-handler",
            service = MessagingHandler.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeNettyStatHandler"
    )
    protected void addNettyStatHandler(MessagingHandler messagingHandler) {
        NettyTransportContextHolder.getInstance().getInterceptor().addHandler(messagingHandler);
    }

    protected void removeNettyStatHandler(MessagingHandler messagingHandler) {
        NettyTransportContextHolder.getInstance().getInterceptor().removeHandler(messagingHandler);
    }

    @Reference(
            name = "netty-log-handler",
            service = MessagingHandler.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeNettyLogHandler"
    )
    protected void addNettyLogHandler(MessagingHandler messagingHandler) {
        NettyTransportContextHolder.getInstance().getInterceptor().addHandler(messagingHandler);
    }

    protected void removeNettyLogHandler(MessagingHandler messagingHandler) {
        NettyTransportContextHolder.getInstance().getInterceptor().removeHandler(messagingHandler);
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        NettyTransportContextHolder.getInstance().getBundleContext().
                registerService(NettyTransportServiceComponent.class, this, null);
        log.info("All CarbonNettyServerInitializers are available");
    }
}
