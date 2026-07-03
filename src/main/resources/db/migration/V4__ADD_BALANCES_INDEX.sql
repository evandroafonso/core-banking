CREATE INDEX idx_balances_account_currency
    ON balances (account_id, currency);