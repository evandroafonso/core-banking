package com.tuum.corebanking.balance.service;

import com.tuum.corebanking.balance.converter.BalanceConverter;
import com.tuum.corebanking.balance.dto.response.BalanceResponse;
import com.tuum.corebanking.balance.event.BalanceCreatedEvent;
import com.tuum.corebanking.balance.event.BalanceUpdateEvent;
import com.tuum.corebanking.balance.mapper.BalanceMapper;
import com.tuum.corebanking.balance.model.Balance;
import com.tuum.corebanking.balance.model.Currency;
import com.tuum.corebanking.exception.AccountNotFoundException;
import com.tuum.corebanking.messaging.event.OperationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceService {

    private final BalanceMapper balanceMapper;
    private final BalanceConverter balanceConverter;
    private final ApplicationEventPublisher applicationEventPublisher;
    
    @Transactional
    public List<BalanceResponse> create(List<Currency> currencies, Long accountId) {
        log.info("Creating balances for account ID: {} with currencies: {}", accountId, currencies);
        List<Balance> balances = balanceConverter.toEntities(currencies, accountId);
        balances.forEach(balanceMapper::insert);
        List<BalanceResponse> responses = balanceConverter.toResponses(balances);
        responses.forEach(this::publishCreatedEvent);
        log.info("Balances created successfully for account ID: {}", accountId);
        return responses;
    }

    public List<BalanceResponse> findByAccountId(Long accountId) {
        log.debug("Finding balances for account ID: {}", accountId);
        List<Balance> balances = balanceMapper.findByAccountId(accountId);
        return balanceConverter.toResponses(balances);
    }

    @Transactional
    public Balance findBalanceWithLock(Long accountId, UUID accountBusinessId, Currency currency) {
        log.debug("Finding balance with lock for account ID: {}, business ID: {}, currency: {}", accountId, accountBusinessId, currency);
        return balanceMapper
                .findByAccountIdAndCurrencyForUpdate(accountId, currency)
                .orElseThrow(() -> new AccountNotFoundException(
                        "No balance found for account %s with currency %s"
                                .formatted(accountBusinessId, currency)
                ));
    }

    @Transactional
    public void update(Balance balance, BigDecimal balanceAfter) {
        log.debug("Updating balance ID: {} from {} to {}", balance.getBusinessId(), balance.getAvailableAmount(), balanceAfter);
        int rowsAffected = balanceMapper.updateAvailableAmount(balance.getId(), balanceAfter);

        if (rowsAffected == 0) {
            log.error("Failed to update balance, no rows affected for id: {}", balance.getBusinessId());
            throw new IllegalStateException("Failed to update balance, no rows affected for id: %s".formatted(balance.getBusinessId()));
        }

        publishBalanceEvent(balanceAfter, balance.getCurrency());
        log.debug("Balance updated successfully for ID: {}", balance.getBusinessId());
    }

    private void publishCreatedEvent(BalanceResponse response) {
        BalanceCreatedEvent event = new BalanceCreatedEvent(
                "BalanceCreated",
                OperationType.INSERT,
                response
        );
        applicationEventPublisher.publishEvent(event);
    }

    private void publishBalanceEvent(BigDecimal balanceAfter, Currency currency) {
        BalanceUpdateEvent event = new BalanceUpdateEvent(
                "BalanceUpdated",
                OperationType.UPDATE,
                new BalanceResponse(balanceAfter, currency)
        );
        applicationEventPublisher.publishEvent(event);
    }


}
