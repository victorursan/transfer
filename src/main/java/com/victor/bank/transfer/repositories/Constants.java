package com.victor.bank.transfer.repositories;

public class Constants {
    public static final String CREATE_CLIENT_TABLE_SQL = "CREATE TABLE IF NOT EXISTS clients ( " +
        "id LONG IDENTITY PRIMARY KEY, " +
        "fullName TEXT NOT NULL, " +
        "balance DOUBLE NOT NULL " +
        ")";
    public static final String CREATE_TRANSACTIONS_TABLE_SQL = "CREATE TABLE IF NOT EXISTS transactions ( " +
        "id LONG IDENTITY PRIMARY KEY, " +
        "fromId LONG NOT NULL, " +
        "toId LONG NOT NULL, " +
        "amount DOUBLE NOT NULL " +
        ")";
}
