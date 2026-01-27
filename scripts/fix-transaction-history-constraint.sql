-- Corrige a constraint transaction_history_transaction_type_check para incluir STORE_TRANSACTION.
-- Execute quando receber: "new row for relation transaction_history violates check constraint transaction_history_transaction_type_check"
-- Exemplo (Docker): docker-compose exec -T postgres psql -U shido -d vehicle-sales-manager < scripts/fix-transaction-history-constraint.sql

ALTER TABLE transaction_history DROP CONSTRAINT IF EXISTS transaction_history_transaction_type_check;
ALTER TABLE transaction_history ADD CONSTRAINT transaction_history_transaction_type_check
  CHECK (transaction_type IN ('PURCHASE', 'SALE', 'EXCHANGE', 'STORE_TRANSACTION'));
