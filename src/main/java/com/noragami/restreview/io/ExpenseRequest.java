package com.noragami.restreview.io;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ExpenseRequest {
    private String description;
    private BigDecimal amount;
    private String paidByEmail;
    private String category;
}
