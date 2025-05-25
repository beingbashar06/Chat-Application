# Java Chat Application with GUI and MySQL Integration

This project is a simple Java-based chat application featuring a graphical user interface (GUI) and MySQL database integration. It supports multiple users, message sending, and a basic client-server architecture using Java Swing and JDBC.

## Project Structure

- `ClientGUI.java` — Client-side user interface for sending/receiving messages.
- `ServerGUI.java` — Server-side GUI for monitoring incoming connections and messages.
- `JdbcConnectivity.java` — Handles MySQL database connection and operations.
- `database.sql` — SQL script to set up the required database and tables.

## Features

- User registration and login
- Real-time messaging between users
- GUI for both client and server
- Persistent storage of users and messages in MySQL
- Timestamped message tracking

## Technologies Used

- Java (Swing for GUI)
- JDBC (Java Database Connectivity)
- MySQL
- SQL

## How to Run

### 1. Database Setup

1. Start your MySQL server.
2. Execute the `database.sql` script:

```bash
javac ServerGUI.java ClientGUI.java JdbcConnectivity.java
java ServerGUI
java ClientGUI


## DataBase Scheme
CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE messages (
  id INT AUTO_INCREMENT PRIMARY KEY,
  sender_id INT,
  receiver_id INT,
  message TEXT NOT NULL,
  sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (sender_id) REFERENCES users(id),
  FOREIGN KEY (receiver_id) REFERENCES users(id)
);

## Licence
This project is licensed under the MIT License.

