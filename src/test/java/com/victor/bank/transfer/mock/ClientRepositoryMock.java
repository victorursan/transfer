package com.victor.bank.transfer.mock;

import com.victor.bank.transfer.repositories.ClientRepository;
import com.victor.bank.transfer.repositories.models.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ClientRepositoryMock implements ClientRepository {

    private List<Client> clients = new ArrayList<>();
    private AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public CompletableFuture<Optional<Client>> addClient(String fullName, Double initialBalance) {
        var client = new Client(idGenerator.getAndIncrement(), fullName, initialBalance);
        clients.add(client);
        return CompletableFuture.completedFuture(Optional.of(client));
    }

    @Override
    public CompletableFuture<Optional<Client>> getClient(Long clientId) {
        return CompletableFuture.completedFuture(clients.stream().filter(c -> c.getId().equals(clientId)).findFirst());
    }

    @Override
    public CompletableFuture<Optional<Long>> updateClientBalance(Long clientId, Double newBalance) {
        clients = clients.stream().map(c -> {
            if (c.getId().equals(clientId)) {
                return new Client(clientId, c.getFullName(), newBalance);
            }
            return c;
        }).collect(Collectors.toList());
        return CompletableFuture.completedFuture(Optional.of(clientId));
    }
}
