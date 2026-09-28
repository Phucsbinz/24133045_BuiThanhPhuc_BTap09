-- Nang cap database VD1/VD2 dang co len VD3. Sao luu database truoc khi chay.
CREATE DATABASE IF NOT EXISTS `btap09_vd12`
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `btap09_vd12`;

-- User cu la tai khoan da ton tai truoc khi bat buoc xac minh email.
SET @add_email_verified = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'email_verified') = 0,
    'ALTER TABLE `users` ADD COLUMN `email_verified` BOOLEAN NOT NULL DEFAULT TRUE',
    'SELECT 1'
);
PREPARE add_email_verified_stmt FROM @add_email_verified;
EXECUTE add_email_verified_stmt;
DEALLOCATE PREPARE add_email_verified_stmt;

CREATE TABLE IF NOT EXISTS `products` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(200) NOT NULL,
    `description` VARCHAR(5000) NULL,
    `price` DECIMAL(18,2) NOT NULL,
    `image_url` VARCHAR(1000) NULL,
    `cloudinary_public_id` VARCHAR(255) NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `user_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`), KEY `idx_products_name` (`name`), KEY `idx_products_user_id` (`user_id`),
    CONSTRAINT `fk_products_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `otp_tokens` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `email` VARCHAR(150) NOT NULL,
    `otp_hash` VARCHAR(100) NOT NULL,
    `otp_type` VARCHAR(30) NOT NULL,
    `expires_at` DATETIME NOT NULL,
    `attempts` INT NOT NULL DEFAULT 0,
    `used` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`), KEY `idx_otp_email_type_created` (`email`,`otp_type`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
