package com.victor.bank.transfer.repositories;

import com.victor.bank.transfer.datastores.DataStore;
import com.victor.bank.transfer.repositories.models.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ClientRepositoryImpl implements ClientRepository {
    private final static Logger log = LoggerFactory.getLogger(ClientRepositoryImpl.class);

    private final DataStore datastore;

    public ClientRepositoryImpl(final DataStore datastore) {
        this.datastore = datastore;
    }

    @Override
    public CompletableFuture<Optional<Client>> addClient(final String fullName, final Double initialBalance) {
        return datastore.executeInsert("INSERT INTO clients(fullName, balance) values ( ?, ? )", preparedStatement -> {
            try {
                preparedStatement.setString(1, fullName);
                preparedStatement.setDouble(2, initialBalance);
            } catch (SQLException sqle) {
                log.error("Failed to set parameters to insert in table 'clients'.", sqle);
            }
            return preparedStatement;
        }, rs -> {
            try {
                final Long cId = rs.getLong("id");
                return Optional.of(new Client(cId, fullName, initialBalance));
            } catch (SQLException sqle) {
                log.error("Failed to read new row client id.", sqle);
                return Optional.empty();
            }
        }).thenApply(this::getOnlyItem);
    }

    private Optional<Client> getOnlyItem(final List<Client> list) {
        if (list.size() != 1) {
            log.error(String.format("Expected one client, got: %s", list.size()));
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }

    @Override
    public CompletableFuture<Optional<Client>> getClient(final Long clientId) {
        return datastore.runQuery("SELECT id, fullName, balance FROM clients WHERE id = ?;", preparedStatement -> {
            try {
                preparedStatement.setLong(1, clientId);
            } catch (SQLException sqle) {
                log.error("Failed to set parameters to read from table 'clients'.", sqle);
            }
            return preparedStatement;
        }, rs -> {
            try {
                final Long cid = rs.getLong("id");
                final String fullName = rs.getString("fullName");
                final Double balance = rs.getDouble("balance");
                return Optional.of(new Client(cid, fullName, balance));
            } catch (SQLException sqle) {
                log.error("Failed to read rows from table 'clients'.", sqle);
                return Optional.empty();
            }
        }).thenApply(this::getOnlyItem);
    }

    @Override
    public CompletableFuture<Optional<Long>> updateClientBalance(final Long clientId, final Double newBalance) {
        return datastore.executeUpdate("UPDATE clients SET balance = ? WHERE id = ?;", preparedStatement -> {
            try {
                preparedStatement.setDouble(1, newBalance);
                preparedStatement.setLong(2, clientId);
            } catch (SQLException sqle) {
                log.error("Failed to set parameters to update table 'clients'.", sqle);
            }
            return preparedStatement;
        }).thenApply(rows -> {
            if (rows == 0 || rows > 1) {
                log.error(String.format("Expected to update one client, actual: %s", rows));
                return Optional.empty();
            }
            return Optional.of(clientId);
        });
    }


}
