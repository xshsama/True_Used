CREATE TABLE IF NOT EXISTS `user_follows` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `follower_id` BIGINT NOT NULL,
  `followed_id` BIGINT NOT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  CONSTRAINT `uk_user_follows_pair` UNIQUE (`follower_id`, `followed_id`),
  CONSTRAINT `fk_user_follows_follower` FOREIGN KEY (`follower_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_user_follows_followed` FOREIGN KEY (`followed_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX `idx_user_follows_follower` ON `user_follows` (`follower_id`);
CREATE INDEX `idx_user_follows_followed` ON `user_follows` (`followed_id`);
