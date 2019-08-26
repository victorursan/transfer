package com.victor.bank.transfer.repositories;

import com.victor.bank.transfer.repositories.models.Transaction;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface TransactionRepository {
    CompletableFuture<Optional<Transaction>> addTransaction(final Long fromId, final Long toId, final Double amount);

    CompletableFuture<Optional<Long>> deleteTransaction(final Long transactionId);

    CompletableFuture<Optional<Transaction>> getTransaction(final Long transferId);
}
