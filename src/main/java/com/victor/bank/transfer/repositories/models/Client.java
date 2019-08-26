package com.victor.bank.transfer.repositories.models;

import lombok.Data;

@Data
public class Client {
    private final Long id;
    private final String fullName;
    private final Double balance;
}
