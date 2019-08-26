package com.victor.bank.transfer.configs;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.List;

import static com.victor.bank.transfer.repositories.Constants.CREATE_CLIENT_TABLE_SQL;
import static com.victor.bank.transfer.repositories.Constants.CREATE_TRANSACTIONS_TABLE_SQL;

public class ConfigsProvider {
    private static Config conf = ConfigFactory.parseResources("application.conf");

    public static H2InMemoryConfig getH2InMemoryConfig() {
        final Config h2MemConf = conf.getConfig("db.h2.mem");
        final String url = h2MemConf.getString("url");
        final int poolSize = h2MemConf.getInt("poolSize");
        return H2InMemoryConfig.of(url, poolSize, List.of(CREATE_CLIENT_TABLE_SQL, CREATE_TRANSACTIONS_TABLE_SQL));
    }

    public static UndertowServerConfig getUndertowServerConfig() {
        final Config undertowConf = conf.getConfig("http.server.undertow");
        final String host = undertowConf.getString("host");
        final int port = undertowConf.getInt("port");
        final int ioThreads = undertowConf.getInt("ioThreads");
        final int workerThreads = undertowConf.getInt("workerThreads");
        return UndertowServerConfig.of(host, port, ioThreads, workerThreads);
    }

}
