package com.victor.bank.transfer.validators;

import com.victor.bank.transfer.models.requests.AddClientReq;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ClientReqValidator {
    private final static Predicate<String> fullNameMatcher = Pattern.compile("\\S+\\s+\\S+").asMatchPredicate().negate();
    private final static String FULL_NAME_VIOLATION = "The full name of the client must be formed by at least 2 words separated by a space.";
    private final static String NEGATIVE_BALANCE = "The initial Balance can't be negative.";

    public static List<String> validateAddReq(final AddClientReq addClientReq) {
        final List<String> validations = new ArrayList<>();

        if (fullNameMatcher.test(addClientReq.getFullName())) {
            validations.add(FULL_NAME_VIOLATION);
        }
        addClientReq.getBalance().filter(balance -> balance < 0).ifPresent(b -> validations.add(NEGATIVE_BALANCE));

        return validations;
    }
}
