package com.victor.bank.transfer.services;

import com.victor.bank.transfer.models.requests.AddClientReq;
import com.victor.bank.transfer.models.response.ClientResp;
import com.victor.bank.transfer.repositories.ClientRepository;
import com.victor.bank.transfer.repositories.models.Client;
import com.victor.bank.transfer.utils.Exceptions;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepo;

    public ClientServiceImpl(final ClientRepository clientRepo) {
        this.clientRepo = clientRepo;
    }

    @Override
    public CompletableFuture<ClientResp> addClient(final AddClientReq newClient) {
        return clientRepo.addClient(newClient.getFullName(), newClient.getBalance().orElse(0.0))
            .thenCompose(this::mapAddClient);
    }

    @Override
    public CompletableFuture<ClientResp> getClient(final Long clientId) {
        return clientRepo.getClient(clientId).thenCompose(this::mapGetClient);
    }

    @NotNull
    private CompletionStage<ClientResp> mapGetClient(final Optional<Client> clientOpt) {
        return mapClientStage(clientOpt, Exceptions.CLIENT_MISSING);
    }

    @NotNull
    private CompletionStage<ClientResp> mapAddClient(final Optional<Client> clientOpt) {
        return mapClientStage(clientOpt, Exceptions.CANNOT_CREATE_CLIENT);
    }

    @NotNull
    private CompletionStage<ClientResp> mapClientStage(final Optional<Client> clientOpt, final Throwable emptyReason) {
        return clientOpt.map(c -> CompletableFuture.completedStage(new ClientResp(c.getId(), c.getFullName(), c.getBalance())))
            .orElse(CompletableFuture.failedStage(emptyReason));
    }

}
