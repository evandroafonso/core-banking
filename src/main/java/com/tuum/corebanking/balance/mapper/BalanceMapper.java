package com.tuum.corebanking.balance.mapper;

import com.tuum.corebanking.balance.model.Balance;
import com.tuum.corebanking.balance.model.Currency;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Mapper
public interface BalanceMapper {

    void insert(Balance balance);

    List<Balance> findByAccountId(@Param("accountId") Long accountId);

    Optional<Balance> findByAccountIdAndCurrencyForUpdate(
            @Param("accountId") Long accountId,
            @Param("currency") Currency currency
    );

    int updateBalance(@Param("id") Long id, @Param("balance") BigDecimal balance);
}