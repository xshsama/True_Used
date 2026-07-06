SET @schema_name = DATABASE();

SET @order_transaction_duplicate_count = (
  SELECT COUNT(1)
  FROM (
    SELECT `transaction_id`
    FROM `orders`
    WHERE `transaction_id` IS NOT NULL AND `transaction_id` <> ''
    GROUP BY `transaction_id`
    HAVING COUNT(1) > 1
  ) duplicated_transactions
);

SET @order_transaction_unique_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'orders'
    AND INDEX_NAME = 'uk_orders_transaction_id'
);

SET @sql = IF(
  @order_transaction_unique_exists = 0 AND @order_transaction_duplicate_count > 0,
  'SIGNAL SQLSTATE ''45000'' SET MESSAGE_TEXT = ''Duplicate orders.transaction_id values must be resolved before creating uk_orders_transaction_id''',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  @order_transaction_unique_exists = 0 AND @order_transaction_duplicate_count = 0,
  'CREATE UNIQUE INDEX `uk_orders_transaction_id` ON `orders` (`transaction_id`)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @products_status_idx_exists = (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = @schema_name
    AND TABLE_NAME = 'products'
    AND INDEX_NAME = 'idx_products_status_id'
);

SET @sql = IF(
  @products_status_idx_exists = 0,
  'CREATE INDEX `idx_products_status_id` ON `products` (`status`, `id`)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
