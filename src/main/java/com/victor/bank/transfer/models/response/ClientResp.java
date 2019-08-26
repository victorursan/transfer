package com.victor.bank.transfer.models.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientResp {
    private Long id;
    private String name;
    private Double balance;
}
