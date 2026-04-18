-- Bảng sys_role (từ entity Role và BaseInformation)
CREATE TABLE tbl_role (
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
CREATE TABLE tbl_user (
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
    PRIMARY KEY (user_id, role_id)
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
CREATE TABLE tbl_refresh_token (
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
    project_value NUMERIC
);

-- Bảng tbl_task (từ entity Task)
CREATE TABLE tbl_task
(
    id                 UUID PRIMARY KEY,
    created_by         VARCHAR(255),
    created_date       TIMESTAMP,
    last_modified_by   VARCHAR(255),
    last_modified_date TIMESTAMP,
    voided             BOOLEAN DEFAULT FALSE,
    name               VARCHAR(255),
    description        TEXT,
    short_description  VARCHAR(255),
    code               VARCHAR(100) UNIQUE NOT NULL,
    project_id         UUID,
    status             VARCHAR(50),
    kanban_order       INTEGER,
    priority           VARCHAR(50),
    estimated_hours    numeric,
    actual_hours       numeric,
    assigned_id        UUID,
    label              VARCHAR(50),
    type               VARCHAR(50)
);

CREATE TABLE tbl_task_log
(
    id                 UUID PRIMARY KEY,
    created_by         VARCHAR(255),
    created_date       TIMESTAMP,
    last_modified_by   VARCHAR(255),
    last_modified_date TIMESTAMP,
    voided             BOOLEAN DEFAULT FALSE,
    task_id            UUID NOT NULL,
    action             VARCHAR(50) NOT NULL,
    old_value          TEXT,
    new_value          TEXT
);


CREATE TABLE tbl_app_param
(
    id                 UUID PRIMARY KEY,
    created_by         VARCHAR(255),
    created_date       TIMESTAMP,
    last_modified_by   VARCHAR(255),
    last_modified_date TIMESTAMP,
    voided             BOOLEAN DEFAULT FALSE,
    description        TEXT,
    param_group        VARCHAR(100),
    param_name         VARCHAR(255),
    param_value        VARCHAR(255),
    param_type         VARCHAR(50)
);

CREATE TABLE resource_allocation
(
    id                 UUID PRIMARY KEY,
    created_by         VARCHAR(255),
    created_date       TIMESTAMP,
    last_modified_by   VARCHAR(255),
    last_modified_date TIMESTAMP,
    voided             BOOLEAN DEFAULT FALSE,
    user_id            UUID NOT NULL,
    role               VARCHAR(50),
    month              TIMESTAMP,
    start_date         TIMESTAMP,
    end_date           TIMESTAMP,
    allocation_percent NUMERIC
);

CREATE TABLE employee_ot
(
    id                 UUID PRIMARY KEY,
    created_by         VARCHAR(255),
    created_date       TIMESTAMP,
    last_modified_by   VARCHAR(255),
    last_modified_date TIMESTAMP,
    voided             BOOLEAN DEFAULT FALSE,
    user_id            UUID NOT NULL,
    project_id         UUID,
    ot_date            TIMESTAMP NOT NULL,
    start_time         TIMESTAMP,
    end_time           TIMESTAMP,
    ot_hours           NUMERIC,
    ot_type            VARCHAR(50), -- weekday / weekend / holiday
    reason             TEXT,
    status             VARCHAR(30), -- draft / submitted / approved / rejected
    approved_by        UUID,
    approved_date      TIMESTAMP
);