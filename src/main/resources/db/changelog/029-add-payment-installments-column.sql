--liquibase formatted sql

--changeset hub:029-add-payment-installments-column
-- Add payment_installments column to contracts table

ALTER TABLE contracts
ADD COLUMN payment_installments TEXT;

-- rollback ALTER TABLE contracts DROP COLUMN payment_installments;