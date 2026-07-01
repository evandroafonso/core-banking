package com.tuum.corebanking.balance.model;

import com.tuum.corebanking.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Balance extends BaseEntity {

    private Long accountId;
    private Currency currency;
    private BigDecimal balance = BigDecimal.ZERO;

}
