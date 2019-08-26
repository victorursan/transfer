package com.victor.bank.transfer.utils;

public final class Exceptions {
    public static Exception CLIENT_MISSING = new Exception("Client not found.");
    public static Exception CANNOT_CREATE_CLIENT = new Exception("Could not create client.");
    public static Exception CANNOT_CREATE_TRANSACTION = new Exception("Could not create transaction.");
    public static Exception NOT_ENOUGH_FUNDS = new Exception("Client doesn't have enough funds.");
    public static Exception FAILED_UPDATE_BALANCE = new Exception("Failed to update client's balance");

}
