package com.noragami.restreview.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryWiseSpent {
    private String category;
    private BigDecimal totalPaid;
    private BigDecimal totalOwed;
    private BigDecimal actualSpent;
}
