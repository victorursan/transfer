package com.victor.bank.transfer.mock;

import com.victor.bank.transfer.repositories.TransactionRepository;
import com.victor.bank.transfer.repositories.models.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public class TransactionRepositoryMock implements TransactionRepository {

    private List<Transaction> transactions = new ArrayList<>();
    private AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public CompletableFuture<Optional<Transaction>> addTransaction(Long fromId, Long toId, Double amount) {
        var toAdd = new Transaction(idGenerator.getAndIncrement(), fromId, toId, amount);
        transactions.add(toAdd);
        return CompletableFuture.completedFuture(Optional.of(toAdd));
    }

    @Override
    public CompletableFuture<Optional<Long>> deleteTransaction(Long transactionId) {
        if (transactions.removeIf(t -> t.getId().equals(transactionId))) {
            return CompletableFuture.completedFuture(Optional.of(transactionId));
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletableFuture<Optional<Transaction>> getTransaction(Long transferId) {
        return CompletableFuture.completedFuture(transactions.stream().filter(t -> t.getId().equals(transferId)).findFirst());
    }
}
