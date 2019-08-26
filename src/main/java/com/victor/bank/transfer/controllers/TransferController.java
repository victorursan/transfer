package com.victor.bank.transfer.controllers;

import io.undertow.server.HttpServerExchange;

public interface TransferController {

    void addTransfer(final HttpServerExchange exchange);
}
