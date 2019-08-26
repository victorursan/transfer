package com.victor.bank.transfer.repositories;

import com.victor.bank.transfer.datastores.DataStore;
import com.victor.bank.transfer.repositories.models.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class TransactionRepositoryImpl implements TransactionRepository {
    private static final Logger log = LoggerFactory.getLogger(TransactionRepositoryImpl.class);

    private final DataStore datastore;

    public TransactionRepositoryImpl(final DataStore datastore) {
        this.datastore = datastore;
    }

    @Override
    public CompletableFuture<Optional<Transaction>> addTransaction(final Long fromId, final Long toId, final Double amount) {
        return datastore.executeInsert("INSERT INTO transactions(fromId, toId, amount) values ( ?, ?, ? )", preparedStatement -> {
            try {
                preparedStatement.setLong(1, fromId);
                preparedStatement.setLong(2, toId);
                preparedStatement.setDouble(3, amount);
            } catch (SQLException sqle) {
                log.error("Failed to set parameters to insert in table 'transactions'.", sqle);
            }
            return preparedStatement;
        }, rs -> {
            try {
                final Long tId = rs.getLong("id");
                return Optional.of(new Transaction(tId, fromId, toId, amount));
            } catch (SQLException sqle) {
                log.error("Failed to read new row transaction id.", sqle);
                return Optional.empty();
            }
        }).thenApply(this::getOnlyItem);
    }

    @Override
    public CompletableFuture<Optional<Long>> deleteTransaction(final Long transactionId) {
        return datastore.executeUpdate("DELETE FROM transactions WHERE id = ?;", preparedStatement -> {
            try {
                preparedStatement.setLong(1, transactionId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return preparedStatement;
        }).thenApply(rows -> {
            if (rows != 1) {
                // log size is wrong
                return Optional.empty();
            }
            return Optional.of(transactionId);

        });
    }


    @Override
    public CompletableFuture<Optional<Transaction>> getTransaction(final Long transactionId) {
        return datastore.runQuery("SELECT id, fromId, toId, amount FROM transactions WHERE id = ?;", preparedStatement -> {
            try {
                preparedStatement.setLong(1, transactionId);
            } catch (SQLException sqle) {
                log.error("Failed to set parameters to insert in table 'clients'.", sqle);
            }
            return preparedStatement;
        }, rs -> {
            try {
                final Long tId = rs.getLong("id");
                final Long fromId = rs.getLong("fromId");
                final Long toId = rs.getLong("toId");
                final Double amount = rs.getDouble("amount");
                return Optional.of(new Transaction(tId, fromId, toId, amount));
            } catch (SQLException sqle) {
                log.error("Failed to read rows from table 'transactions'.", sqle);
                return Optional.empty();
            }
        }).thenApply(this::getOnlyItem);
    }

    private Optional<Transaction> getOnlyItem(final List<Transaction> list) {
        if (list.size() != 1) {
            log.error(String.format("Expected one transaction, got: %s", list.size()));
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }
}
