/*
 * database.sql - Schema definition for Chat Application Database
 *
 * Creates tables for user accounts and message history with proper relationships
 * Uses utf8mb4 character set to support full Unicode including emojis
 */

-- Create the database with Unicode support
CREATE DATABASE chat_application
    DEFAULT CHARACTER SET = 'utf8mb4';

-- Switch to the newly created database
USE chat_application;

/*
 * users table - Stores registered user accounts
 * Fields:
 *   id - Auto-incrementing primary key
 *   username - Unique identifier for login (case-sensitive)
 *   password - Hashed password storage (recommend using bcrypt/PBKDF2)
 *   created_at - Automatic timestamp of account creation
 */
CREATE TABLE users(
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT 'Unique username for login',
    password VARCHAR(100) NOT NULL COMMENT 'Hashed password string',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Account creation time'
) COMMENT='Stores registered user accounts';

/*
 * messages table - Records all chat messages
 * Fields:
 *   id - Auto-incrementing primary key
 *   sender_id - References the user who sent the message
 *   receiver_id - References the recipient user (NULL for group messages)
 *   message - The actual message content
 *   sent_at - Automatic timestamp of when message was sent
 *
 * Foreign Keys:
 *   sender_id links to users.id
 *   receiver_id links to users.id
 */
CREATE TABLE messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL COMMENT 'User who sent the message',
    receiver_id INT COMMENT 'NULL indicates group message',
    message TEXT NOT NULL COMMENT 'Message content',
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When message was sent',
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE SET NULL
) COMMENT='Stores all chat messages with sender/receiver info';

-- Optional: Create indexes for better performance on frequent queries
CREATE INDEX idx_messages_sender ON messages(sender_id);
CREATE INDEX idx_messages_receiver ON messages(receiver_id);
CREATE INDEX idx_messages_timestamp ON messages(sent_at);
