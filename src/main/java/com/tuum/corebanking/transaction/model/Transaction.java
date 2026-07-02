package com.tuum.corebanking.transaction.model;

import com.tuum.corebanking.balance.model.Currency;
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
public class Transaction extends BaseEntity {

    private Long accountId;
    private BigDecimal amount;
    private Currency currency;
    private Direction direction;
    private String description;
    private BigDecimal balanceAfter;
}