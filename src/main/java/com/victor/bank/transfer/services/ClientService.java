package com.victor.bank.transfer.services;

import com.victor.bank.transfer.models.requests.AddClientReq;
import com.victor.bank.transfer.models.response.ClientResp;

import java.util.concurrent.CompletableFuture;

public interface ClientService {

    CompletableFuture<ClientResp> addClient(final AddClientReq client);

    CompletableFuture<ClientResp> getClient(final Long clientId);
}
