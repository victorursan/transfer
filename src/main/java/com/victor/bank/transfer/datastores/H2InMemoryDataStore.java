package com.victor.bank.transfer.datastores;

import com.victor.bank.transfer.configs.H2InMemoryConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class H2InMemoryDataStore implements DataStore {

    private final HikariDataSource dataSource;
    private final ExecutorService es;

    public H2InMemoryDataStore(final H2InMemoryConfig config) {
        final HikariConfig hikariConfig = new HikariConfig();

        es = Executors.newWorkStealingPool(config.getPoolSize());
        hikariConfig.setMaximumPoolSize(config.getPoolSize());

        hikariConfig.setJdbcUrl(config.getUrl());
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(hikariConfig);

        try (final Connection conn = dataSource.getConnection();
             final Statement stm = conn.createStatement()) {
            for (String query : config.getInitQueries()) {
                stm.execute(query);
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    @Override
    public <T> CompletableFuture<T> withConnection(final Function<Connection, T> connectionHandler) {
        return CompletableFuture.supplyAsync(() -> {
            try (final Connection conn = dataSource.getConnection()) {
                return connectionHandler.apply(conn);
            } catch (SQLException sqle) {
                sqle.printStackTrace();
                return null;
            }
        }, es);
    }


    @Override
    public void close() throws IOException {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
