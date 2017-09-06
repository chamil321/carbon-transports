/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.transport.http.netty.contractimpl;

import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.ssl.SSLConfig;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.contract.HttpClientConnector;
import org.wso2.carbon.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.carbon.transport.http.netty.contract.ServerConnector;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketClientConnector;
import org.wso2.carbon.transport.http.netty.contractimpl.websocket.WebSocketClientConnectorImpl;
import org.wso2.carbon.transport.http.netty.contractimpl.websocket.WebSocketMessageImpl;
import org.wso2.carbon.transport.http.netty.listener.ServerBootstrapConfiguration;
import org.wso2.carbon.transport.http.netty.listener.ServerConnectorBootstrap;
import org.wso2.carbon.transport.http.netty.listener.WebSocketSourceHandler;
import org.wso2.carbon.transport.http.netty.sender.channel.BootstrapConfiguration;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.util.Map;

/**
 * Implementation of HttpWsConnectorFactory interface
 */
public class HttpWsConnectorFactoryImpl implements HttpWsConnectorFactory {

    @Override
    public ServerConnector createServerConnector(ServerBootstrapConfiguration serverBootstrapConfiguration,
            ListenerConfiguration listenerConfig) {
        ServerConnectorBootstrap serverConnectorBootstrap = new ServerConnectorBootstrap();
        serverConnectorBootstrap.addSocketConfiguration(serverBootstrapConfiguration);
        serverConnectorBootstrap.addSecurity(listenerConfig.getSslConfig());
        serverConnectorBootstrap.addIdleTimeout(listenerConfig.getSocketIdleTimeout(120000));
        serverConnectorBootstrap.addHttpTraceLogHandler(listenerConfig.isHttpTraceLogEnabled());
        serverConnectorBootstrap.addChunkedWriteHandler(listenerConfig.isChunkedDisabled());
        serverConnectorBootstrap.addThreadPools(Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().availableProcessors() * 2);

        return serverConnectorBootstrap.getServerConnector(listenerConfig.getHost(), listenerConfig.getPort());
    }

    @Override
    public HttpClientConnector createHttpClientConnector(Map<String, Object> transportProperties,
            SenderConfiguration senderConfiguration) {
        SSLConfig sslConfig = senderConfiguration.getSslConfig();
        int socketIdleTimeout = senderConfiguration.getSocketIdleTimeout(60000);
        boolean httpTraceLogEnabled = senderConfiguration.isHttpTraceLogEnabled();
        boolean chunkedDisabled = senderConfiguration.isChunkedDisabled();

        ConnectionManager.init(transportProperties);
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        BootstrapConfiguration.createBootStrapConfiguration(transportProperties);

        return new HttpClientConnectorImpl(connectionManager, sslConfig, socketIdleTimeout, httpTraceLogEnabled
                , chunkedDisabled);
    }

    @Override
    public WebSocketClientConnector createWsClientConnector(Map<String, Object> senderProperties) {
        String subProtocol = (String) senderProperties.get(Constants.WEBSOCKET_SUBPROTOCOLS);
        String remoteUrl = (String) senderProperties.get(Constants.REMOTE_ADDRESS);
        String target = (String) senderProperties.get(Constants.TO);

        boolean allowExtensions = true;
        Object allowExtensionsObj = senderProperties.get(Constants.WEBSOCKET_ALLOW_EXTENSIONS);
        if (allowExtensionsObj != null) {
            allowExtensions = (boolean) allowExtensionsObj;
        }

        WebSocketSourceHandler sourceHandler = null;
        WebSocketMessageImpl webSocketMessage =
                (WebSocketMessageImpl) senderProperties.get(Constants.WEBSOCKET_MESSAGE);
        if (webSocketMessage != null) {
            sourceHandler =
                    (WebSocketSourceHandler) webSocketMessage.getProperty(Constants.SRC_HANDLER);
        }

        return new WebSocketClientConnectorImpl(remoteUrl, target, subProtocol, allowExtensions, sourceHandler);
    }
}
