package com.victor.bank.transfer.models.response;

import lombok.Data;

@Data(staticConstructor = "of")
public class ErrorResp {
    private final String message;
}
