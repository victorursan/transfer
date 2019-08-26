package com.victor.bank.transfer.models.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddClientReq {
    private String fullName;
    private Optional<Double> balance = Optional.empty();
}
