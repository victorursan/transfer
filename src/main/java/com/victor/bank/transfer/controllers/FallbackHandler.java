package com.victor.bank.transfer.controllers;

import com.victor.bank.transfer.utils.UndertowUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.util.Map;

public class FallbackHandler implements HttpHandler {

    @Override
    public void handleRequest(final HttpServerExchange exchange) {
        final String method = exchange.getRequestMethod().toString();
        final String path = exchange.getRequestPath();
        final Map<String, String> reason = Map.of(
            "reason", "Unsupported route.",
            "method", method,
            "path", path);
        UndertowUtils.respondJson(exchange, StatusCodes.BAD_REQUEST, reason);
    }
}
