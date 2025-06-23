package com.noragami.restreview.io;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OweRecordDTO {
    private String fromUser;
    private String toUser;
    private BigDecimal amount;
}
