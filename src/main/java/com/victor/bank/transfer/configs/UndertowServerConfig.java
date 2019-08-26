package com.victor.bank.transfer.configs;

import lombok.Data;

@Data(staticConstructor = "of")
public class UndertowServerConfig {
    private final String host;
    private final int port;
    private final int ioThreads;
    private final int workerThreads;

}
