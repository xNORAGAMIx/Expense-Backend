package com.noragami.restreview.io;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBalanceDTO {
    private String userEmail;
    private BigDecimal netBalance;
}
