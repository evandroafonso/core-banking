package com.tuum.corebanking.transaction.mapper;

import com.tuum.corebanking.transaction.model.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TransactionMapper {

    void insert(Transaction transaction);

    List<Transaction> findByAccountId(@Param("accountId") Long accountId);
}
