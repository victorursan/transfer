package com.victor.bank.transfer.services;

import com.victor.bank.transfer.models.requests.TransferReq;
import com.victor.bank.transfer.models.response.TransferResp;

import java.util.concurrent.CompletableFuture;

public interface TransferService {

    CompletableFuture<TransferResp> transfer(final TransferReq transferReq);

}
