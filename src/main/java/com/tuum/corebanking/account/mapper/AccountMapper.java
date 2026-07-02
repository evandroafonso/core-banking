package com.tuum.corebanking.account.mapper;

import com.tuum.corebanking.account.model.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface AccountMapper {

    void insert(Account account);

    Optional<Account> findById(@Param("id") Long id);

    Optional<Account> findByBusinessId(@Param("businessId") UUID businessId);

    Optional<Long> findAccountIdByBusinessId(@Param("businessId") UUID businessId);

    List<Account> findByCustomerId(@Param("customerId") UUID customerId);
}