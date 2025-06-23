package com.noragami.restreview.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpentSummaryDTO {
    private BigDecimal totalActualSpent;
    private List<GroupWiseSpent> groupWise;
    private List<CategoryWiseSpent> categoryWise;
}
