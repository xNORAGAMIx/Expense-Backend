package com.noragami.restreview.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementResponse {
    private String groupName;
    private String toEmail;
    private String toName;
    private String fromEmail;
    private String fromName;
    private BigDecimal amount;
    private Timestamp settledAt;
}
