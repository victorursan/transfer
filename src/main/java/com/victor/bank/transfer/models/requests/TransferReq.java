package com.victor.bank.transfer.models.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferReq {
    private Long fromId;
    private Long toId;
    private Double amount;
}
