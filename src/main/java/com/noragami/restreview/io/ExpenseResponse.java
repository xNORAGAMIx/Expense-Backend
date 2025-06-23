package com.noragami.restreview.io;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Builder
public class ExpenseResponse {
    private String description;
    private BigDecimal amount;
    private String paidByEmail;
    private String paidByName;
    private Timestamp createdAt;
}
