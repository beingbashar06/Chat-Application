/* Enhanced database schema with user authentication and message persistence */
CREATE DATABASE IF NOT EXISTS chat_application
    DEFAULT CHARACTER SET = 'utf8mb4'
    DEFAULT COLLATE = 'utf8mb4_unicode_ci';

USE chat_application;

-- Users table with authentication fields
CREATE TABLE IF NOT EXISTS users(
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,  -- For bcrypt hashed passwords
    salt VARCHAR(255),  -- For password hashing
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    is_online BOOLEAN DEFAULT FALSE,
    CONSTRAINT username_format CHECK (username REGEXP '^[a-zA-Z0-9_]{3,20}$')
) COMMENT='Stores registered user accounts';

-- Enhanced messages table
CREATE TABLE IF NOT EXISTS messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NULL COMMENT 'NULL for public messages',
    message_type ENUM('PUBLIC', 'PRIVATE', 'SYSTEM') NOT NULL,
    message TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_delivered BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT message_length CHECK (LENGTH(message) BETWEEN 1 AND 2000)
) COMMENT='Stores all chat messages';

-- Indexes for performance
CREATE INDEX idx_messages_sender ON messages(sender_id);
CREATE INDEX idx_messages_receiver ON messages(receiver_id);
CREATE INDEX idx_messages_timestamp ON messages(sent_at);
CREATE INDEX idx_messages_type ON messages(message_type);