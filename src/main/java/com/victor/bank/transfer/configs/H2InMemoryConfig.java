package com.victor.bank.transfer.configs;

import lombok.Data;

import java.util.List;

@Data(staticConstructor = "of")
public class H2InMemoryConfig {
    private final String url;
    private final int poolSize;
    private final List<String> initQueries;
}
