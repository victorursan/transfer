package com.victor.bank.transfer.validators;

import com.victor.bank.transfer.models.requests.TransferReq;
import org.junit.jupiter.api.Test;

import static com.victor.bank.transfer.validators.TransferReqValidator.NEGATIVE_AMOUNT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

class TransferReqValidatorTest {

    @Test
    void validateAddReq() {
       var validTransfer =  TransferReqValidator.validateAddReq(new TransferReq(1L, 2L, 10d));
       var invalidTransfer =  TransferReqValidator.validateAddReq(new TransferReq(1L, 2L, -10d));
       assertThat(validTransfer, hasSize(0));
       assertThat(invalidTransfer, hasSize(1));
       assertThat(invalidTransfer, contains(NEGATIVE_AMOUNT));
    }
}