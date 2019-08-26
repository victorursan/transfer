package com.victor.bank.transfer.repositories;

import com.victor.bank.transfer.repositories.models.Client;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ClientRepository {
    CompletableFuture<Optional<Client>> addClient(final String fullName, final Double initialBalance);

    CompletableFuture<Optional<Client>> getClient(final Long clientId);

    CompletableFuture<Optional<Long>> updateClientBalance(final Long clientId, final Double newBalance);
}
