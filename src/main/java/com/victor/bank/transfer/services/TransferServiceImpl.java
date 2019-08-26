package com.victor.bank.transfer.services;

import com.victor.bank.transfer.models.requests.TransferReq;
import com.victor.bank.transfer.models.response.TransferResp;
import com.victor.bank.transfer.repositories.ClientRepository;
import com.victor.bank.transfer.repositories.TransactionRepository;
import com.victor.bank.transfer.repositories.models.Client;
import com.victor.bank.transfer.repositories.models.Transaction;
import com.victor.bank.transfer.utils.Exceptions;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;

public class TransferServiceImpl implements TransferService {
    private static final Logger log = LoggerFactory.getLogger(TransferServiceImpl.class);

    private final ConcurrentHashMap<Long, StampedLock> customerLocks = new ConcurrentHashMap<>();

    private final TransactionRepository transactions;
    private final ClientRepository clients;

    public TransferServiceImpl(final TransactionRepository transactions, final ClientRepository clients) {
        this.transactions = transactions;
        this.clients = clients;
    }


    @Override
    public CompletableFuture<TransferResp> transfer(final TransferReq transferReq) {
        return transactions.addTransaction(transferReq.getFromId(), transferReq.getToId(), transferReq.getAmount())
            .thenComposeAsync(transactionOpt -> transactionOpt.map(transaction ->
                performTransaction(transaction)
                    .exceptionallyCompose(err -> {
                        log.error("Could not perform transaction.", err);
                        transactions.deleteTransaction(transaction.getId()).whenComplete((tOpt, err2) -> {
                                if (err2 != null || tOpt.isEmpty()) {
                                    log.error("Failed to delete transaction", err2);
                                }
                            }
                        );
                        return CompletableFuture.failedStage(err);
                    })
            ).orElse(CompletableFuture.failedStage(Exceptions.CANNOT_CREATE_TRANSACTION)))
            .thenApply(this::mapTransfer);
    }

    private CompletionStage<? extends Transaction> performTransaction(final Transaction transaction) {
        return modifyCustomerAmount(transaction.getFromId(), subtractAmount(transaction.getAmount()))
            .thenCompose(client -> modifyCustomerAmount(transaction.getToId(), addAmount(transaction.getAmount()))
                .exceptionallyComposeAsync(t -> modifyCustomerAmount(transaction.getFromId(), addAmount(transaction.getAmount()))
                    .thenCompose(c -> CompletableFuture.failedStage(t))))
            .thenApply(c -> transaction);
    }

    private CompletionStage<? extends Client> modifyCustomerAmount(final Long customerID, final Function<? super Client, CompletionStage<? extends Client>> operation) {
        final StampedLock rl = customerLocks.computeIfAbsent(customerID, unused -> new StampedLock());
        final long stamp = rl.writeLock();
        return clients.getClient(customerID)
            .thenCompose(clientOpt -> clientOpt.map(operation).orElse(CompletableFuture.failedStage(Exceptions.CLIENT_MISSING)))
            .thenCompose(this::updateClientBalance)
            .whenComplete((c, t) -> rl.unlock(stamp));
    }

    @NotNull
    private CompletionStage<? extends Client> updateClientBalance(final Client client) {
        return clients.updateClientBalance(client.getId(), client.getBalance())
            .thenCompose(opt -> opt.map(l -> CompletableFuture.completedStage(client))
                .orElse(CompletableFuture.failedStage(Exceptions.FAILED_UPDATE_BALANCE)));
    }

    @NotNull
    private Function<? super Client, CompletionStage<? extends Client>> subtractAmount(final Double changeAmount) {
        return client -> {
            if (client.getBalance() < changeAmount) {
                return CompletableFuture.failedStage(Exceptions.NOT_ENOUGH_FUNDS);
            }
            return CompletableFuture.completedStage(new Client(client.getId(), client.getFullName(), client.getBalance() - changeAmount));
        };
    }

    @NotNull
    private Function<? super Client, CompletionStage<? extends Client>> addAmount(final Double changeAmount) {
        return client -> CompletableFuture.completedStage(new Client(client.getId(), client.getFullName(), client.getBalance() + changeAmount));
    }

    @NotNull
    private TransferResp mapTransfer(final Transaction transaction) {
        return new TransferResp(transaction.getId(), transaction.getFromId(), transaction.getToId(), transaction.getAmount());
    }
}
