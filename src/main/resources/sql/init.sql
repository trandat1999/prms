
-- Bảng Role
CREATE TABLE roles (
    name VARCHAR(50) PRIMARY KEY,
    description VARCHAR(255)
);

-- Bảng User
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    phone_number VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);

-- Bảng trung gian User_Roles
CREATE TABLE user_roles (
    user_id VARCHAR(36) NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role_name),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_name) REFERENCES roles(name) ON DELETE CASCADE
);

-- Bảng Invalidated Token (Lưu các token đã logout)
CREATE TABLE invalidated_tokens (
    id VARCHAR(36) PRIMARY KEY,
    expiry_time TIMESTAMP NOT NULL
);

-- Dữ liệu mẫu ban đầu
INSERT INTO roles (name, description) VALUES ('ADMIN', 'Quản trị viên hệ thống');
INSERT INTO roles (name, description) VALUES ('USER', 'Người dùng thông thường');
