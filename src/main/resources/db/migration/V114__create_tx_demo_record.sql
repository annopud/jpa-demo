-- Create table for transaction demo records used by TxDemoRecord entity
-- Drop if exists to make the migration idempotent for local testing
DROP TABLE IF EXISTS tx_demo_record;

CREATE TABLE tx_demo_record (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tag VARCHAR(255) NOT NULL COMMENT 'Identifiable tag for the demo',
  create_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) COMMENT='Table used by TransactionDemoService for propagation demos';

-- Insert a seed row to make it easy to observe initial state in demos
INSERT INTO tx_demo_record (tag, create_date) VALUES ('seed-1', CURRENT_TIMESTAMP);

