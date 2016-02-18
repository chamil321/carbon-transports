package org.wso2.carbon.transport.http.netty.logger.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.messaging.MessagingHandler;
import org.wso2.carbon.transport.http.netty.logger.LoggerHandler;

/**
 * OSGi BundleActivator of the Netty transport component.
 */
public class NettyTransportLogActivator implements BundleActivator {
    @Override public void start(BundleContext bundleContext) throws Exception {
        bundleContext.registerService(MessagingHandler.class, new LoggerHandler(), null);
    }

    @Override public void stop(BundleContext bundleContext) throws Exception {

    }
}
