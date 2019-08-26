package com.victor.bank.transfer;

import com.victor.bank.transfer.configs.UndertowServerConfig;
import com.victor.bank.transfer.controllers.ClientController;
import com.victor.bank.transfer.controllers.FallbackHandler;
import com.victor.bank.transfer.controllers.HealthHandler;
import com.victor.bank.transfer.controllers.TransferController;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;

public final class HttpServer {
    private static final HttpHandler healthHandler = new HealthHandler();
    private static final HttpHandler fallbackHandler = new FallbackHandler();
    private final ClientController clientCtrl;
    private final TransferController transferCtrl;
    private final Undertow server;

    public HttpServer(final UndertowServerConfig undertowConfig, final ClientController clientController, final TransferController transferController) {
        clientCtrl = clientController;
        transferCtrl = transferController;
        server = Undertow.builder()
            .addHttpListener(undertowConfig.getPort(), undertowConfig.getHost())
            .setWorkerThreads(undertowConfig.getWorkerThreads())
            .setIoThreads(undertowConfig.getIoThreads())
            .setHandler(createPathHandlers())
            .build();
    }

    public void start() {
        server.start();
    }

    private RoutingHandler createPathHandlers() {
        return new RoutingHandler()
            .get("/health", healthHandler)
            .post("/clients", clientCtrl::addClient)
            .get("/clients/{clientId}", clientCtrl::getClient)
            .post("/transfers", transferCtrl::addTransfer)
            .setFallbackHandler(fallbackHandler);
    }

    public void stop() {
        server.stop();
    }
}
