-- ===================================================================
-- BTAP09 - Dữ liệu mẫu (Sample Data)
-- Mật khẩu mặc định của tất cả tài khoản là: 123456
-- BCrypt Hash: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
-- ===================================================================

USE `btap09_vd12`;

-- 1. Nạp Roles
INSERT INTO `roles` (`id`, `name`) VALUES
(1, 'ROLE_ADMIN'),
(2, 'ROLE_USER')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- 2. Nạp Users
-- Admin: admin / trungnh@hcmute.edu.vn / 123456
INSERT INTO `users` (`id`, `username`, `email`, `password`, `full_name`, `images`, `enabled`, `created_at`, `role_id`) VALUES
(1, 'admin', 'trungnh@hcmute.edu.vn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ThS. Nguyễn Hữu Trung', '/images/user.png', TRUE, NOW(), 1),

-- User 01: user01 / user01@gmail.com / 123456 (Có ảnh)
(2, 'user01', 'user01@gmail.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Nguyễn Hữu Trung', '/images/user.png', TRUE, NOW(), 2),

-- User 02: user02 / phuc.bui@example.com / 123456 (Không có ảnh, kiểm thử fallback avatar)
(3, 'user02', 'phuc.bui@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Bùi Thanh Phúc', NULL, TRUE, NOW(), 2),

-- Disabled User: disabled_user / locked@example.com / 123456 (Tài khoản bị khóa)
(4, 'disabled_user', 'locked@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Tài Khoản Bị Khóa', NULL, FALSE, NOW(), 2)
ON DUPLICATE KEY UPDATE `full_name` = VALUES(`full_name`);
