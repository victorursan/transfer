package com.victor.bank.transfer.controllers;

import com.victor.bank.transfer.models.requests.TransferReq;
import com.victor.bank.transfer.services.TransferService;
import com.victor.bank.transfer.utils.UndertowUtils;
import com.victor.bank.transfer.validators.TransferReqValidator;
import io.undertow.io.Receiver.FullBytesCallback;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import static com.victor.bank.transfer.utils.UndertowUtils.validateAndRunAsync;

public class TransferControllerImpl implements TransferController {

    private final TransferService transferService;

    public TransferControllerImpl(final TransferService transferService) {
        this.transferService = transferService;
    }

    @Override
    public void addTransfer(final HttpServerExchange exchange) {
        final FullBytesCallback callback = validateAndRunAsync(TransferReq.class, TransferReqValidator::validateAddReq,
            transferService::transfer, StatusCodes.CREATED);
        exchange.getRequestReceiver().receiveFullBytes(callback, UndertowUtils::getErrorCallback);
    }

}
