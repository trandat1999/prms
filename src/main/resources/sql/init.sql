-- Bảng sys_role (từ entity Role và BaseInformation)
CREATE TABLE sys_role (
    id UUID PRIMARY KEY,
    created_by VARCHAR(255),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(255),
    last_modified_date TIMESTAMP,
    voided BOOLEAN DEFAULT FALSE,
    name VARCHAR(255),
    description VARCHAR(255),
    short_description VARCHAR(255),
    code VARCHAR(100) UNIQUE NOT NULL
);

-- Bảng sys_user (từ entity User)
CREATE TABLE sys_user (
    id UUID PRIMARY KEY,
    created_by VARCHAR(255),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(255),
    last_modified_date TIMESTAMP,
    voided BOOLEAN DEFAULT FALSE,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    full_name VARCHAR(255),
    account_non_expired BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    credentials_non_expired BOOLEAN DEFAULT TRUE,
    enabled BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP
);

-- Bảng trung gian tbl_user_role
CREATE TABLE tbl_user_role (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
);

-- Bảng tbl_token (từ entity Token)
CREATE TABLE tbl_token (
    id UUID PRIMARY KEY,
    created_by VARCHAR(255),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(255),
    last_modified_date TIMESTAMP,
    voided BOOLEAN DEFAULT FALSE,
    token VARCHAR(255) UNIQUE,
    type VARCHAR(50) DEFAULT 'BEARER',
    revoked BOOLEAN DEFAULT FALSE,
    expired BOOLEAN DEFAULT FALSE,
    username VARCHAR(100)
);

-- Bảng refresh_token (từ entity RefreshToken)
CREATE TABLE refresh_token (
    id UUID PRIMARY KEY,
    created_by VARCHAR(255),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(255),
    last_modified_date TIMESTAMP,
    voided BOOLEAN DEFAULT FALSE,
    token VARCHAR(255),
    username VARCHAR(100),
    revoked BOOLEAN DEFAULT FALSE,
    expired TIMESTAMP
);

-- Dữ liệu mặc định (Nếu không khởi tạo từ code)
-- Role Supper Admin: 550e8400-e29b-41d4-a716-446655440000
-- User supper_admin: 550e8400-e29b-41d4-a716-446655440001
-- Mật khẩu mặc định: 123456 (BCrypt: $2a$10$N.zSPrv.h6Hh6S9D.T0F8.G1W5eG5VfG9/3R.F.3.T.f.f.f.f.f.f.)
-- Lưu ý: Tốt nhất nên để DataInitializer tự khởi tạo để đảm bảo BCrypt mã hóa đúng.

-- Bảng tbl_project (từ entity Project)
CREATE TABLE tbl_project (
    id UUID PRIMARY KEY,
    created_by VARCHAR(255),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(255),
    last_modified_date TIMESTAMP,
    voided BOOLEAN DEFAULT FALSE,
    name VARCHAR(255),
    description TEXT,
    short_description VARCHAR(255),
    code VARCHAR(100) UNIQUE NOT NULL,
    manager_id UUID,
    priority VARCHAR(50),
    status VARCHAR(50),
    progress_percentage DOUBLE PRECISION,
    start_date DATE,
    end_date DATE,
    CONSTRAINT fk_project_manager FOREIGN KEY (manager_id) REFERENCES sys_user(id)
);

-- Bảng tbl_task (từ entity Task)
CREATE TABLE tbl_task (
    id UUID PRIMARY KEY,
    created_by VARCHAR(255),
    created_date TIMESTAMP,
    last_modified_by VARCHAR(255),
    last_modified_date TIMESTAMP,
    voided BOOLEAN DEFAULT FALSE,
    name VARCHAR(255),
    description TEXT,
    short_description VARCHAR(255),
    code VARCHAR(100) UNIQUE NOT NULL,
    project_id UUID,
    status VARCHAR(50),
    priority VARCHAR(50),
    estimated_hours DOUBLE PRECISION,
    actual_hours DOUBLE PRECISION,
    due_date DATE,
    CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES tbl_project(id)
);

-- Bảng liên kết task_assignment
CREATE TABLE task_assignment (
    task_id UUID NOT NULL,
    user_id UUID NOT NULL,
    PRIMARY KEY (task_id, user_id),
    CONSTRAINT fk_assignment_task FOREIGN KEY (task_id) REFERENCES tbl_task(id) ON DELETE CASCADE,
    CONSTRAINT fk_assignment_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
);
