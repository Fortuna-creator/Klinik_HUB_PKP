-- liquibase formatted sql

-- changeset hub:add-total-payment-amount-to-consultation-history
ALTER TABLE consultation_history ADD COLUMN total_payment_amount DECIMAL(19,2);
-- rollback ALTER TABLE consultation_history DROP COLUMN total_payment_amount;