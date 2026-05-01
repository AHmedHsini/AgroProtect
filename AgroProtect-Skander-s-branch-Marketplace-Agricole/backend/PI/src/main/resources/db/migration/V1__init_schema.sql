-- =====================================================
-- V1: Initial Schema for User & Identity Service
-- AgriPlatform - Microfinance & Microinsurance System
-- =====================================================

-- Users table - Core user information
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    uuid VARCHAR(36) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    status ENUM('PENDING', 'ACTIVE', 'LOCKED', 'DISABLED', 'DELETED') DEFAULT 'PENDING',
    auth_provider ENUM('LOCAL', 'GOOGLE', 'PHONE') DEFAULT 'LOCAL',
    google_id VARCHAR(255) UNIQUE,
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    mfa_enabled BOOLEAN DEFAULT FALSE,
    biometric_enabled BOOLEAN DEFAULT FALSE,
    failed_login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP NULL,
    password_changed_at TIMESTAMP NULL,
    last_login_at TIMESTAMP NULL,
    consent_marketing BOOLEAN DEFAULT FALSE,
    consent_data_processing BOOLEAN DEFAULT FALSE,
    consent_policy_version VARCHAR(20),
    consent_accepted_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_users_email (email),
    INDEX idx_users_phone (phone),
    INDEX idx_users_uuid (uuid),
    INDEX idx_users_status (status),
    INDEX idx_users_google_id (google_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Roles table
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    is_system_role BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Permissions table
CREATE TABLE permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(255),
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_permissions_resource (resource)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User-Role junction table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT NULL,
    
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Role-Permission junction table
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Refresh tokens table
CREATE TABLE refresh_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    device_id VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent TEXT,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    revoked_at TIMESTAMP NULL,
    revoked_reason VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_refresh_tokens_hash (token_hash),
    INDEX idx_refresh_tokens_user (user_id, expires_at),
    INDEX idx_refresh_tokens_device (user_id, device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Device sessions table
CREATE TABLE device_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    device_id VARCHAR(255) NOT NULL,
    device_name VARCHAR(255),
    device_type ENUM('WEB', 'MOBILE', 'TABLET', 'DESKTOP', 'UNKNOWN') DEFAULT 'UNKNOWN',
    os_name VARCHAR(100),
    os_version VARCHAR(50),
    browser_name VARCHAR(100),
    browser_version VARCHAR(50),
    ip_address VARCHAR(45),
    location_country VARCHAR(100),
    location_city VARCHAR(100),
    user_agent TEXT,
    last_active_at TIMESTAMP,
    is_current BOOLEAN DEFAULT FALSE,
    is_trusted BOOLEAN DEFAULT FALSE,
    revoked BOOLEAN DEFAULT FALSE,
    revoked_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_device_sessions_user (user_id),
    INDEX idx_device_sessions_device (device_id),
    UNIQUE INDEX idx_device_sessions_user_device (user_id, device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Biometric data table (stores encrypted face embeddings only)
CREATE TABLE biometric_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE NOT NULL,
    biometric_type ENUM('FACE') DEFAULT 'FACE',
    embedding_encrypted BLOB NOT NULL,
    encryption_iv VARCHAR(32) NOT NULL,
    encryption_tag VARCHAR(32) NOT NULL,
    liveness_verified BOOLEAN DEFAULT FALSE,
    quality_score DECIMAL(5,4),
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_verified_at TIMESTAMP NULL,
    verification_count INT DEFAULT 0,
    failed_verification_count INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Password history table (for preventing password reuse)
CREATE TABLE password_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_password_history_user (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Email verification tokens
CREATE TABLE email_verification_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_email_verification_token (token_hash),
    INDEX idx_email_verification_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Password reset tokens
CREATE TABLE password_reset_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_password_reset_token (token_hash),
    INDEX idx_password_reset_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Audit logs table
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    actor_id BIGINT,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100),
    resource_id VARCHAR(100),
    ip_address VARCHAR(45),
    user_agent TEXT,
    device_id VARCHAR(255),
    status ENUM('SUCCESS', 'FAILURE', 'BLOCKED') NOT NULL,
    failure_reason VARCHAR(255),
    details JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_audit_logs_user (user_id),
    INDEX idx_audit_logs_action (action),
    INDEX idx_audit_logs_created (created_at),
    INDEX idx_audit_logs_resource (resource_type, resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Service accounts table (for service-to-service authentication)
CREATE TABLE service_accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    service_name VARCHAR(100) UNIQUE NOT NULL,
    api_key_hash VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    permissions JSON,
    is_active BOOLEAN DEFAULT TRUE,
    last_used_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_service_accounts_name (service_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Seed Data: Default Roles and Permissions
-- =====================================================

-- Insert default roles
INSERT INTO roles (name, description, is_system_role) VALUES
('ADMIN', 'System administrator with full access', TRUE),
('FARMER', 'Agricultural farmer user', TRUE),
('INVESTOR', 'Microfinance investor', TRUE),
('INSURER', 'Insurance provider representative', TRUE),
('EXPERT', 'Agricultural and financial expert advisor', TRUE),
('WORKER', 'Agricultural worker/laborer', TRUE),
('USER', 'Basic authenticated user', TRUE);

-- Insert default permissions
INSERT INTO permissions (name, description, resource, action) VALUES
-- User permissions
('user:read', 'Read own user profile', 'user', 'read'),
('user:update', 'Update own user profile', 'user', 'update'),
('user:delete', 'Delete own account', 'user', 'delete'),

-- Admin permissions
('admin:users:read', 'Read all users', 'admin', 'users:read'),
('admin:users:update', 'Update any user', 'admin', 'users:update'),
('admin:users:delete', 'Delete any user', 'admin', 'users:delete'),
('admin:roles:manage', 'Manage roles and permissions', 'admin', 'roles:manage'),
('admin:audit:read', 'Read audit logs', 'admin', 'audit:read'),

-- Loan permissions
('loan:apply', 'Apply for loans', 'loan', 'apply'),
('loan:view', 'View loan details', 'loan', 'view'),
('loan:invest', 'Invest in loans', 'loan', 'invest'),
('loan:approve', 'Approve loan applications', 'loan', 'approve'),

-- Insurance permissions
('insurance:apply', 'Apply for insurance', 'insurance', 'apply'),
('insurance:view', 'View insurance policies', 'insurance', 'view'),
('insurance:claim', 'File insurance claims', 'insurance', 'claim'),
('insurance:underwrite', 'Underwrite insurance policies', 'insurance', 'underwrite'),

-- Land/Project permissions
('land:register', 'Register agricultural land', 'land', 'register'),
('land:view', 'View land/project listings', 'land', 'view'),
('project:create', 'Create farm projects', 'project', 'create'),
('project:manage', 'Manage farm projects', 'project', 'manage'),

-- Marketplace permissions
('marketplace:list', 'List items on marketplace', 'marketplace', 'list'),
('marketplace:buy', 'Buy from marketplace', 'marketplace', 'buy'),

-- Advisory permissions
('advisory:request', 'Request expert advice', 'advisory', 'request'),
('advisory:provide', 'Provide expert advice', 'advisory', 'provide'),

-- Internal service permissions
('internal:token:validate', 'Validate tokens (service-to-service)', 'internal', 'token:validate'),
('internal:user:read', 'Read user data (service-to-service)', 'internal', 'user:read');

-- Assign permissions to roles
-- ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'ADMIN';

-- USER gets basic permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'USER' AND p.name IN ('user:read', 'user:update', 'user:delete');

-- FARMER permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'FARMER' AND p.name IN (
    'user:read', 'user:update', 'loan:apply', 'loan:view', 
    'insurance:apply', 'insurance:view', 'insurance:claim',
    'land:register', 'land:view', 'project:create', 'project:manage',
    'marketplace:list', 'marketplace:buy', 'advisory:request'
);

-- INVESTOR permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'INVESTOR' AND p.name IN (
    'user:read', 'user:update', 'loan:view', 'loan:invest',
    'land:view', 'marketplace:buy'
);

-- INSURER permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'INSURER' AND p.name IN (
    'user:read', 'user:update', 'insurance:view', 'insurance:underwrite',
    'land:view'
);

-- EXPERT permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'EXPERT' AND p.name IN (
    'user:read', 'user:update', 'advisory:provide', 'land:view',
    'project:manage', 'loan:approve'
);

-- WORKER permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'WORKER' AND p.name IN (
    'user:read', 'user:update', 'land:view', 'marketplace:buy'
);
