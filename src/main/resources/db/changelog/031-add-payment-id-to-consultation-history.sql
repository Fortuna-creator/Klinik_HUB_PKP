-- Add payment_id column to consultation_history table
ALTER TABLE consultation_history ADD COLUMN payment_id UUID;

-- Add comment for documentation
COMMENT ON COLUMN consultation_history.payment_id IS 'Reference to payment record for payment-related events';