-- Chay tren database btap09_vd12 moi; khong xoa bang hoac du lieu.
CREATE DATABASE IF NOT EXISTS `btap09_vd12`
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `btap09_vd12`;

CREATE TABLE IF NOT EXISTS `roles` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    PRIMARY KEY (`id`), UNIQUE KEY `uk_roles_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `email` VARCHAR(150) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(150) NOT NULL,
    `images` VARCHAR(500) NULL,
    `enabled` BOOLEAN NOT NULL DEFAULT TRUE,
    `email_verified` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `role_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`), UNIQUE KEY `uk_users_username` (`username`),
    UNIQUE KEY `uk_users_email` (`email`),
    CONSTRAINT `fk_users_roles` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
