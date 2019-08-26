package com.victor.bank.transfer.repositories.models;

import lombok.Data;

@Data
public class Transaction {
    private final Long id;
    private final Long fromId;
    private final Long toId;
    private final Double amount;
}
