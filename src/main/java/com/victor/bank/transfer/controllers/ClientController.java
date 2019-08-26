package com.victor.bank.transfer.controllers;

import io.undertow.server.HttpServerExchange;

public interface ClientController {

    void addClient(final HttpServerExchange exchange);

    void getClient(final HttpServerExchange exchange);

}