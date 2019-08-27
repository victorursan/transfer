package com.victor.bank.transfer.validators;

import com.victor.bank.transfer.models.requests.AddClientReq;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.victor.bank.transfer.validators.ClientReqValidator.FULL_NAME_VIOLATION;
import static com.victor.bank.transfer.validators.ClientReqValidator.NEGATIVE_BALANCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

class ClientReqValidatorTest {

    @Test
    void validateAddReq() {
        var validClientReq1 = ClientReqValidator.validateAddReq(new AddClientReq("John Doe", Optional.empty()));
        var validClientReq2 = ClientReqValidator.validateAddReq(new AddClientReq("John Doe", Optional.of(10d)));
        var invalidClientName = ClientReqValidator.validateAddReq(new AddClientReq("JohnDoe", Optional.of(10d)));
        var invalidClientBalance = ClientReqValidator.validateAddReq(new AddClientReq("John Doe", Optional.of(-10d)));
        var invalidClientNameAndBalance = ClientReqValidator.validateAddReq(new AddClientReq("JohnDoe", Optional.of(-10d)));
        assertThat(validClientReq1, hasSize(0));
        assertThat(validClientReq2, hasSize(0));
        assertThat(invalidClientName, hasSize(1));
        assertThat(invalidClientBalance, hasSize(1));
        assertThat(invalidClientNameAndBalance, hasSize(2));
        assertThat(invalidClientName, contains(FULL_NAME_VIOLATION));
        assertThat(invalidClientBalance, contains(NEGATIVE_BALANCE));
        assertThat(invalidClientNameAndBalance, containsInAnyOrder(FULL_NAME_VIOLATION, NEGATIVE_BALANCE));

    }
}