package com.victor.bank.transfer.datastores;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public interface DataStore extends Closeable {
    Logger log = LoggerFactory.getLogger("DataStore");

    <T> CompletableFuture<T> withConnection(final Function<Connection, T> connectionHandler);

    default <T> CompletableFuture<List<T>> runQuery(final String query, final UnaryOperator<PreparedStatement> setParameters, final Function<ResultSet, Optional<T>> rowMapper) {
        return withConnection(conn -> {
            try (final PreparedStatement stmt = setParameters.apply(conn.prepareStatement(query));
                 final ResultSet rs = stmt.executeQuery()) {
                final List<T> result = new ArrayList<>();
                while (rs.next()) {
                    rowMapper.apply(rs).ifPresent(result::add);
                }
                return result;
            } catch (SQLException sqle) {
                log.error(String.format("Failed to run query: '%s'", query), sqle);
                return List.of();
            }
        });
    }

    default <T> CompletableFuture<List<T>> executeInsert(final String query, final UnaryOperator<PreparedStatement> setParameters, final Function<ResultSet, Optional<T>> keyMapper) {
        return withConnection(conn -> {
            try (final PreparedStatement stmt = setParameters.apply(conn.prepareStatement(query, RETURN_GENERATED_KEYS))) {
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating user failed, no rows affected.");
                }
                try (final ResultSet rs = stmt.getGeneratedKeys()) {
                    final List<T> result = new ArrayList<>();
                    while (rs.next()) {
                        keyMapper.apply(rs).ifPresent(result::add);
                    }
                    return result;
                }

            } catch (SQLException sqle) {
                log.error(String.format("Failed to execute insert: '%s'", query), sqle);
                return List.of();
            }
        });

    }

    default CompletableFuture<Integer> executeUpdate(final String query, final UnaryOperator<PreparedStatement> setParameters) {
        return withConnection(conn -> {
            try (final PreparedStatement stmt = setParameters.apply(conn.prepareStatement(query, RETURN_GENERATED_KEYS))) {
                return stmt.executeUpdate();
            } catch (SQLException sqle) {
                log.error(String.format("Failed to execute update: '%s'", query), sqle);
                return 0;
            }
        });
    }

}
