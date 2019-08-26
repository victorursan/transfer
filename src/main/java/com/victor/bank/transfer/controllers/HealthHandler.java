package com.victor.bank.transfer.controllers;

import com.victor.bank.transfer.utils.UndertowUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;


public class HealthHandler implements HttpHandler {
    @Override
    public void handleRequest(final HttpServerExchange exchange) {
        UndertowUtils.respondJson(exchange, StatusCodes.OK, "Ok");
    }
}
