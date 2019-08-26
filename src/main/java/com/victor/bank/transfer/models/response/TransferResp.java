package com.victor.bank.transfer.models.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferResp {
    private Long id;
    private Long fromId;
    private Long toId;
    private Double amount;
}
