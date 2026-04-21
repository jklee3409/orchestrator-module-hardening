-- Re-run this script before each benchmark pass.
-- It seeds only the JMeter bid benchmark namespace.
--
-- What it creates:
-- - benchmark users and sellers
-- - ROLE_USER / TRANSACTION authority mapping
-- - user_pay balances
-- - winner and loser auction feeds for both implementations
-- - one committed loser bid so db-lock loser-heavy also fails fast
--
-- Notes:
-- - JMeter bypass authenticates with Authorization: Bearer {email}.
-- - The CSV password column is kept only for compatibility with JMeter CSV format.
-- - loser-heavy is aligned with fixedBidAmount=10200 from jmeter/bid-benchmark.jmx.

SET @bidder_count = 500;
SET @bidder_pay = 2000000;
SET @seller_pay = 0;
SET @winner_feed_sales_price = 10000;
SET @loser_seed_bid_amount = 10300;
SET @sales_data_amount = 1000;
SET @default_image_number = 1;
SET @expires_at = DATE_ADD(NOW(), INTERVAL 2 DAY);

SET @email_pattern = 'bidbench-%@loadtest.local';
SET @feed_title_pattern = '[JMeter][BidBenchmark]%';

SET @redis_seller_email = 'bidbench-redis-seller@loadtest.local';
SET @redis_seed_email = 'bidbench-redis-seed@loadtest.local';
SET @db_lock_seller_email = 'bidbench-db-lock-seller@loadtest.local';
SET @db_lock_seed_email = 'bidbench-db-lock-seed@loadtest.local';

SET @redis_winner_feed_title = '[JMeter][BidBenchmark][Redis][Winner] Auction';
SET @redis_loser_feed_title = '[JMeter][BidBenchmark][Redis][Loser] Auction';
SET @db_lock_winner_feed_title = '[JMeter][BidBenchmark][DB-Lock][Winner] Auction';
SET @db_lock_loser_feed_title = '[JMeter][BidBenchmark][DB-Lock][Loser] Auction';

DROP TEMPORARY TABLE IF EXISTS `tmp_bidbench_seq`;
CREATE TEMPORARY TABLE `tmp_bidbench_seq` (
    `seq` INT NOT NULL PRIMARY KEY
);

INSERT INTO `tmp_bidbench_seq` (`seq`)
SELECT ones.n + (tens.n * 10) + (hundreds.n * 100) + 1 AS seq
FROM (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
) AS ones
CROSS JOIN (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
) AS tens
CROSS JOIN (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
) AS hundreds
WHERE ones.n + (tens.n * 10) + (hundreds.n * 100) + 1 <= @bidder_count;

INSERT INTO `telecom_company` (`telecom_company_id`, `name`, `created_at`, `updated_at`)
VALUES
    (1, 'SKT', NOW(), NOW()),
    (2, 'KT', NOW(), NOW()),
    (3, 'LG U+', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `updated_at` = VALUES(`updated_at`);

INSERT INTO `status` (`status_id`, `domain`, `code`, `description`, `created_at`, `updated_at`)
VALUES
    (12, 'USER', 'ACTIVE', 'ACTIVE', NOW(), NOW()),
    (14, 'FEED', 'ON_SALE', 'ON_SALE', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    `domain` = VALUES(`domain`),
    `code` = VALUES(`code`),
    `description` = VALUES(`description`),
    `updated_at` = VALUES(`updated_at`);

INSERT INTO `sales_type` (`sales_type_id`, `name`, `created_at`, `updated_at`)
VALUES
    (1, 'NORMAL', NOW(), NOW()),
    (2, 'BID', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `updated_at` = VALUES(`updated_at`);

INSERT INTO `role` (`name`, `created_at`, `updated_at`)
SELECT 'ROLE_USER', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1
    FROM `role` r
    WHERE r.`name` COLLATE utf8mb4_unicode_ci = CONVERT('ROLE_USER' USING utf8mb4) COLLATE utf8mb4_unicode_ci
);

INSERT INTO `authority` (`name`, `created_at`, `updated_at`)
SELECT 'READ', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1
    FROM `authority` a
    WHERE a.`name` COLLATE utf8mb4_unicode_ci = CONVERT('READ' USING utf8mb4) COLLATE utf8mb4_unicode_ci
);

INSERT INTO `authority` (`name`, `created_at`, `updated_at`)
SELECT 'NOTICE', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1
    FROM `authority` a
    WHERE a.`name` COLLATE utf8mb4_unicode_ci = CONVERT('NOTICE' USING utf8mb4) COLLATE utf8mb4_unicode_ci
);

INSERT INTO `authority` (`name`, `created_at`, `updated_at`)
SELECT 'TRANSACTION', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1
    FROM `authority` a
    WHERE a.`name` COLLATE utf8mb4_unicode_ci = CONVERT('TRANSACTION' USING utf8mb4) COLLATE utf8mb4_unicode_ci
);

INSERT INTO `authority` (`name`, `created_at`, `updated_at`)
SELECT 'WRITE', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1
    FROM `authority` a
    WHERE a.`name` COLLATE utf8mb4_unicode_ci = CONVERT('WRITE' USING utf8mb4) COLLATE utf8mb4_unicode_ci
);

INSERT INTO `role_authority` (`role_id`, `authority_id`, `created_at`, `updated_at`)
SELECT r.`role_id`, a.`authority_id`, NOW(), NOW()
FROM `role` r
JOIN `authority` a
    ON a.`name` COLLATE utf8mb4_unicode_ci = CONVERT('READ' USING utf8mb4) COLLATE utf8mb4_unicode_ci
    OR a.`name` COLLATE utf8mb4_unicode_ci = CONVERT('NOTICE' USING utf8mb4) COLLATE utf8mb4_unicode_ci
    OR a.`name` COLLATE utf8mb4_unicode_ci = CONVERT('TRANSACTION' USING utf8mb4) COLLATE utf8mb4_unicode_ci
    OR a.`name` COLLATE utf8mb4_unicode_ci = CONVERT('WRITE' USING utf8mb4) COLLATE utf8mb4_unicode_ci
WHERE r.`name` COLLATE utf8mb4_unicode_ci = CONVERT('ROLE_USER' USING utf8mb4) COLLATE utf8mb4_unicode_ci
  AND NOT EXISTS (
      SELECT 1
      FROM `role_authority` ra
      WHERE ra.`role_id` = r.`role_id`
        AND ra.`authority_id` = a.`authority_id`
  );

DELETE ua
FROM `user_authority` ua
JOIN `user` u
    ON u.`user_id` = ua.`user_id`
WHERE u.`email` COLLATE utf8mb4_unicode_ci LIKE CONVERT(@email_pattern USING utf8mb4) COLLATE utf8mb4_unicode_ci;

DELETE b
FROM `bids` b
JOIN `transaction_feed` tf
    ON tf.`transaction_feed_id` = b.`transaction_feed_id`
WHERE tf.`title` COLLATE utf8mb4_unicode_ci LIKE CONVERT(@feed_title_pattern USING utf8mb4) COLLATE utf8mb4_unicode_ci;

DELETE
FROM `transaction_feed`
WHERE `title` COLLATE utf8mb4_unicode_ci LIKE CONVERT(@feed_title_pattern USING utf8mb4) COLLATE utf8mb4_unicode_ci;

INSERT INTO `user` (
    `created_at`,
    `updated_at`,
    `email`,
    `password`,
    `nickname`,
    `phone_number`,
    `provider`,
    `status_id`,
    `telecom_company_id`
)
SELECT NOW(), NOW(), @redis_seller_email, 'BypassOnly123!', 'bidbench-redis-seller', '01091000001', 'loadtest', 12, 1
WHERE NOT EXISTS (
    SELECT 1
    FROM `user` u
    WHERE u.`email` COLLATE utf8mb4_unicode_ci = CONVERT(@redis_seller_email USING utf8mb4) COLLATE utf8mb4_unicode_ci
);

INSERT INTO `user` (
    `created_at`,
    `updated_at`,
    `email`,
    `password`,
    `nickname`,
    `phone_number`,
    `provider`,
    `status_id`,
    `telecom_company_id`
)
SELECT NOW(), NOW(), @redis_seed_email, 'BypassOnly123!', 'bidbench-redis-seed', '01091000002', 'loadtest', 12, 1
WHERE NOT EXISTS (
    SELECT 1
    FROM `user` u
    WHERE u.`email` COLLATE utf8mb4_unicode_ci = CONVERT(@redis_seed_email USING utf8mb4) COLLATE utf8mb4_unicode_ci
);

INSERT INTO `user` (
    `created_at`,
    `updated_at`,
    `email`,
    `password`,
    `nickname`,
    `phone_number`,
    `provider`,
    `status_id`,
    `telecom_company_id`
)
SELECT NOW(), NOW(), @db_lock_seller_email, 'BypassOnly123!', 'bidbench-db-lock-seller', '01092000001', 'loadtest', 12, 1
WHERE NOT EXISTS (
    SELECT 1
    FROM `user` u
    WHERE u.`email` COLLATE utf8mb4_unicode_ci = CONVERT(@db_lock_seller_email USING utf8mb4) COLLATE utf8mb4_unicode_ci
);

INSERT INTO `user` (
    `created_at`,
    `updated_at`,
    `email`,
    `password`,
    `nickname`,
    `phone_number`,
    `provider`,
    `status_id`,
    `telecom_company_id`
)
SELECT NOW(), NOW(), @db_lock_seed_email, 'BypassOnly123!', 'bidbench-db-lock-seed', '01092000002', 'loadtest', 12, 1
WHERE NOT EXISTS (
    SELECT 1
    FROM `user` u
    WHERE u.`email` COLLATE utf8mb4_unicode_ci = CONVERT(@db_lock_seed_email USING utf8mb4) COLLATE utf8mb4_unicode_ci
);

INSERT INTO `user` (
    `created_at`,
    `updated_at`,
    `email`,
    `password`,
    `nickname`,
    `phone_number`,
    `provider`,
    `status_id`,
    `telecom_company_id`
)
SELECT
    NOW(),
    NOW(),
    CONCAT('bidbench-redis-bidder', LPAD(s.`seq`, 4, '0'), '@loadtest.local'),
    'BypassOnly123!',
    CONCAT('bidbench-redis-bidder', LPAD(s.`seq`, 4, '0')),
    CONCAT('01031', LPAD(s.`seq`, 6, '0')),
    'loadtest',
    12,
    1
FROM `tmp_bidbench_seq` s
WHERE NOT EXISTS (
    SELECT 1
    FROM `user` u
    WHERE u.`email` COLLATE utf8mb4_unicode_ci = CONVERT(CONCAT('bidbench-redis-bidder', LPAD(s.`seq`, 4, '0'), '@loadtest.local') USING utf8mb4) COLLATE utf8mb4_unicode_ci
);

INSERT INTO `user` (
    `created_at`,
    `updated_at`,
    `email`,
    `password`,
    `nickname`,
    `phone_number`,
    `provider`,
    `status_id`,
    `telecom_company_id`
)
SELECT
    NOW(),
    NOW(),
    CONCAT('bidbench-db-lock-bidder', LPAD(s.`seq`, 4, '0'), '@loadtest.local'),
    'BypassOnly123!',
    CONCAT('bidbench-db-lock-bidder', LPAD(s.`seq`, 4, '0')),
    CONCAT('01032', LPAD(s.`seq`, 6, '0')),
    'loadtest',
    12,
    1
FROM `tmp_bidbench_seq` s
WHERE NOT EXISTS (
    SELECT 1
    FROM `user` u
    WHERE u.`email` COLLATE utf8mb4_unicode_ci = CONVERT(CONCAT('bidbench-db-lock-bidder', LPAD(s.`seq`, 4, '0'), '@loadtest.local') USING utf8mb4) COLLATE utf8mb4_unicode_ci
);

UPDATE `user`
SET `password` = 'BypassOnly123!',
    `provider` = 'loadtest',
    `status_id` = 12,
    `telecom_company_id` = 1,
    `updated_at` = NOW()
WHERE `email` COLLATE utf8mb4_unicode_ci LIKE CONVERT(@email_pattern USING utf8mb4) COLLATE utf8mb4_unicode_ci;

INSERT INTO `user_role` (`user_user_id`, `role_id`, `created_at`, `updated_at`)
SELECT u.`user_id`, r.`role_id`, NOW(), NOW()
FROM `user` u
JOIN `role` r
    ON r.`name` COLLATE utf8mb4_unicode_ci = CONVERT('ROLE_USER' USING utf8mb4) COLLATE utf8mb4_unicode_ci
WHERE u.`email` COLLATE utf8mb4_unicode_ci LIKE CONVERT(@email_pattern USING utf8mb4) COLLATE utf8mb4_unicode_ci
  AND NOT EXISTS (
      SELECT 1
      FROM `user_role` ur
      WHERE ur.`user_user_id` = u.`user_id`
        AND ur.`role_id` = r.`role_id`
  );

INSERT INTO `user_pay` (`user_id`, `pay`, `created_at`, `updated_at`)
SELECT
    u.`user_id`,
    CASE
        WHEN u.`email` COLLATE utf8mb4_unicode_ci = CONVERT(@redis_seller_email USING utf8mb4) COLLATE utf8mb4_unicode_ci
          OR u.`email` COLLATE utf8mb4_unicode_ci = CONVERT(@db_lock_seller_email USING utf8mb4) COLLATE utf8mb4_unicode_ci
            THEN @seller_pay
        WHEN u.`email` COLLATE utf8mb4_unicode_ci = CONVERT(@redis_seed_email USING utf8mb4) COLLATE utf8mb4_unicode_ci
          OR u.`email` COLLATE utf8mb4_unicode_ci = CONVERT(@db_lock_seed_email USING utf8mb4) COLLATE utf8mb4_unicode_ci
            THEN @bidder_pay - @loser_seed_bid_amount
        ELSE @bidder_pay
    END,
    NOW(),
    NOW()
FROM `user` u
WHERE u.`email` COLLATE utf8mb4_unicode_ci LIKE CONVERT(@email_pattern USING utf8mb4) COLLATE utf8mb4_unicode_ci
  AND NOT EXISTS (
      SELECT 1
      FROM `user_pay` up
      WHERE up.`user_id` = u.`user_id`
  );

UPDATE `user_pay` up
JOIN `user` u
    ON u.`user_id` = up.`user_id`
SET up.`pay` = CASE
        WHEN u.`email` COLLATE utf8mb4_unicode_ci = CONVERT(@redis_seller_email USING utf8mb4) COLLATE utf8mb4_unicode_ci
          OR u.`email` COLLATE utf8mb4_unicode_ci = CONVERT(@db_lock_seller_email USING utf8mb4) COLLATE utf8mb4_unicode_ci
            THEN @seller_pay
        WHEN u.`email` COLLATE utf8mb4_unicode_ci = CONVERT(@redis_seed_email USING utf8mb4) COLLATE utf8mb4_unicode_ci
          OR u.`email` COLLATE utf8mb4_unicode_ci = CONVERT(@db_lock_seed_email USING utf8mb4) COLLATE utf8mb4_unicode_ci
            THEN @bidder_pay - @loser_seed_bid_amount
        ELSE @bidder_pay
    END,
    up.`updated_at` = NOW()
WHERE u.`email` COLLATE utf8mb4_unicode_ci LIKE CONVERT(@email_pattern USING utf8mb4) COLLATE utf8mb4_unicode_ci;

INSERT INTO `transaction_feed` (
    `created_at`,
    `updated_at`,
    `content`,
    `default_image_number`,
    `expires_at`,
    `is_deleted`,
    `sales_data_amount`,
    `sales_price`,
    `title`,
    `version`,
    `sales_type_id`,
    `status_id`,
    `telecome_company_id`,
    `seller_id`
)
SELECT
    NOW(),
    NOW(),
    'Redis winner benchmark auction',
    @default_image_number,
    @expires_at,
    0,
    @sales_data_amount,
    @winner_feed_sales_price,
    @redis_winner_feed_title,
    0,
    2,
    14,
    1,
    u.`user_id`
FROM `user` u
WHERE u.`email` COLLATE utf8mb4_unicode_ci = CONVERT(@redis_seller_email USING utf8mb4) COLLATE utf8mb4_unicode_ci;

INSERT INTO `transaction_feed` (
    `created_at`,
    `updated_at`,
    `content`,
    `default_image_number`,
    `expires_at`,
    `is_deleted`,
    `sales_data_amount`,
    `sales_price`,
    `title`,
    `version`,
    `sales_type_id`,
    `status_id`,
    `telecome_company_id`,
    `seller_id`
)
SELECT
    NOW(),
    NOW(),
    'Redis loser benchmark auction',
    @default_image_number,
    @expires_at,
    0,
    @sales_data_amount,
    @loser_seed_bid_amount,
    @redis_loser_feed_title,
    0,
    2,
    14,
    1,
    u.`user_id`
FROM `user` u
WHERE u.`email` COLLATE utf8mb4_unicode_ci = CONVERT(@redis_seller_email USING utf8mb4) COLLATE utf8mb4_unicode_ci;

INSERT INTO `transaction_feed` (
    `created_at`,
    `updated_at`,
    `content`,
    `default_image_number`,
    `expires_at`,
    `is_deleted`,
    `sales_data_amount`,
    `sales_price`,
    `title`,
    `version`,
    `sales_type_id`,
    `status_id`,
    `telecome_company_id`,
    `seller_id`
)
SELECT
    NOW(),
    NOW(),
    'DB lock winner benchmark auction',
    @default_image_number,
    @expires_at,
    0,
    @sales_data_amount,
    @winner_feed_sales_price,
    @db_lock_winner_feed_title,
    0,
    2,
    14,
    1,
    u.`user_id`
FROM `user` u
WHERE u.`email` COLLATE utf8mb4_unicode_ci = CONVERT(@db_lock_seller_email USING utf8mb4) COLLATE utf8mb4_unicode_ci;

INSERT INTO `transaction_feed` (
    `created_at`,
    `updated_at`,
    `content`,
    `default_image_number`,
    `expires_at`,
    `is_deleted`,
    `sales_data_amount`,
    `sales_price`,
    `title`,
    `version`,
    `sales_type_id`,
    `status_id`,
    `telecome_company_id`,
    `seller_id`
)
SELECT
    NOW(),
    NOW(),
    'DB lock loser benchmark auction',
    @default_image_number,
    @expires_at,
    0,
    @sales_data_amount,
    @loser_seed_bid_amount,
    @db_lock_loser_feed_title,
    0,
    2,
    14,
    1,
    u.`user_id`
FROM `user` u
WHERE u.`email` COLLATE utf8mb4_unicode_ci = CONVERT(@db_lock_seller_email USING utf8mb4) COLLATE utf8mb4_unicode_ci;

INSERT INTO `bids` (
    `created_at`,
    `updated_at`,
    `transaction_feed_id`,
    `user_id`,
    `bid_amount`,
    `bid_time`
)
SELECT
    NOW(),
    NOW(),
    tf.`transaction_feed_id`,
    u.`user_id`,
    @loser_seed_bid_amount,
    NOW()
FROM `transaction_feed` tf
JOIN `user` u
    ON u.`email` COLLATE utf8mb4_unicode_ci = CONVERT(@redis_seed_email USING utf8mb4) COLLATE utf8mb4_unicode_ci
WHERE tf.`title` COLLATE utf8mb4_unicode_ci = CONVERT(@redis_loser_feed_title USING utf8mb4) COLLATE utf8mb4_unicode_ci;

INSERT INTO `bids` (
    `created_at`,
    `updated_at`,
    `transaction_feed_id`,
    `user_id`,
    `bid_amount`,
    `bid_time`
)
SELECT
    NOW(),
    NOW(),
    tf.`transaction_feed_id`,
    u.`user_id`,
    @loser_seed_bid_amount,
    NOW()
FROM `transaction_feed` tf
JOIN `user` u
    ON u.`email` COLLATE utf8mb4_unicode_ci = CONVERT(@db_lock_seed_email USING utf8mb4) COLLATE utf8mb4_unicode_ci
WHERE tf.`title` COLLATE utf8mb4_unicode_ci = CONVERT(@db_lock_loser_feed_title USING utf8mb4) COLLATE utf8mb4_unicode_ci;

SELECT 'redis_winner_feed_id' AS `label`, CAST(tf.`transaction_feed_id` AS CHAR) AS `value`
FROM `transaction_feed` tf
WHERE tf.`title` COLLATE utf8mb4_unicode_ci = CONVERT(@redis_winner_feed_title USING utf8mb4) COLLATE utf8mb4_unicode_ci
UNION ALL
SELECT 'redis_loser_feed_id' AS `label`, CAST(tf.`transaction_feed_id` AS CHAR) AS `value`
FROM `transaction_feed` tf
WHERE tf.`title` COLLATE utf8mb4_unicode_ci = CONVERT(@redis_loser_feed_title USING utf8mb4) COLLATE utf8mb4_unicode_ci
UNION ALL
SELECT 'db_lock_winner_feed_id' AS `label`, CAST(tf.`transaction_feed_id` AS CHAR) AS `value`
FROM `transaction_feed` tf
WHERE tf.`title` COLLATE utf8mb4_unicode_ci = CONVERT(@db_lock_winner_feed_title USING utf8mb4) COLLATE utf8mb4_unicode_ci
UNION ALL
SELECT 'db_lock_loser_feed_id' AS `label`, CAST(tf.`transaction_feed_id` AS CHAR) AS `value`
FROM `transaction_feed` tf
WHERE tf.`title` COLLATE utf8mb4_unicode_ci = CONVERT(@db_lock_loser_feed_title USING utf8mb4) COLLATE utf8mb4_unicode_ci
UNION ALL
SELECT 'redis_users_csv' AS `label`, 'redis-bidders.csv' AS `value`
UNION ALL
SELECT 'db_lock_users_csv' AS `label`, 'db-lock-bidders.csv' AS `value`
UNION ALL
SELECT 'jmeter_test_key' AS `label`, 'local-bid-hotpath' AS `value`
UNION ALL
SELECT 'loser_seed_bid_amount' AS `label`, CAST(@loser_seed_bid_amount AS CHAR) AS `value`;

DROP TEMPORARY TABLE IF EXISTS `tmp_bidbench_seq`;
