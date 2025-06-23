package com.noragami.restreview.io;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SettlementRequest {
    private String toEmail;
    private BigDecimal amount;
}
