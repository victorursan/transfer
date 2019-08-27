package com.victor.bank.transfer.validators;

import com.victor.bank.transfer.models.requests.TransferReq;

import java.util.List;

public class TransferReqValidator {
    public final static String NEGATIVE_AMOUNT = "Can't transfer a zero or negative amount.";

    public static List<String> validateAddReq(final TransferReq transferReq) {
        if (transferReq.getAmount() <= 0) {
            return List.of(NEGATIVE_AMOUNT);
        }
        return List.of();
    }
}
