-- ===================================================================
-- BTAP09 - Spring Security 7 & Spring Boot 4 (VD1 & VD2)
-- Sinh viên: Bùi Thanh Phúc - MSSV: 24133045
-- Cơ sở dữ liệu: MySQL 8.0 (btap09_vd12)
-- ===================================================================

CREATE DATABASE IF NOT EXISTS `btap09_vd12`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `btap09_vd12`;

-- 1. Bảng Roles (Vai trò)
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `roles`;

CREATE TABLE `roles` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng Users (Người dùng)
CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL,
    `email` VARCHAR(150) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(150) NOT NULL,
    `images` VARCHAR(500) NULL,
    `enabled` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `role_id` BIGINT NOT NULL,
    CONSTRAINT `uk_users_username` UNIQUE (`username`),
    CONSTRAINT `uk_users_email` UNIQUE (`email`),
    CONSTRAINT `fk_users_roles` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
