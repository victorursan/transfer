package com.victor.bank.transfer.controllers;

import com.victor.bank.transfer.models.requests.AddClientReq;
import com.victor.bank.transfer.services.ClientService;
import com.victor.bank.transfer.utils.UndertowUtils;
import com.victor.bank.transfer.validators.ClientReqValidator;
import io.undertow.io.Receiver.FullBytesCallback;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.PathTemplateMatch;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.victor.bank.transfer.utils.UndertowUtils.buildResponse;
import static com.victor.bank.transfer.utils.UndertowUtils.respondJson;
import static com.victor.bank.transfer.utils.UndertowUtils.runAsync;
import static com.victor.bank.transfer.utils.UndertowUtils.validateAndRunAsync;

public class ClientControllerImpl implements ClientController {
    private static final Logger log = LoggerFactory.getLogger(ClientControllerImpl.class);

    private final ClientService clientService;

    public ClientControllerImpl(final ClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public void addClient(final HttpServerExchange exchange) {
        final FullBytesCallback callback = validateAndRunAsync(AddClientReq.class, ClientReqValidator::validateAddReq,
            clientService::addClient, StatusCodes.CREATED);
        exchange.getRequestReceiver().receiveFullBytes(callback, UndertowUtils::getErrorCallback);
    }

    @Override
    public void getClient(final HttpServerExchange exchange) {
        final PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
        final String clientIdStr = pathMatch.getParameters().get("clientId");
        getClient(exchange, clientIdStr);
    }

    private void getClient(final HttpServerExchange exchange, final String clientIdStr) {
        try {
            final Long clientId = Long.parseLong(clientIdStr);
            runAsync(exchange, buildResponse(clientService.getClient(clientId), StatusCodes.OK));
        } catch (Exception e) {
            final String message = String.format("{clientId: %s} miss-formatted or missing.", clientIdStr);
            log.error(message, e);
            respondJson(exchange, StatusCodes.BAD_REQUEST, message);
        }
    }

}
