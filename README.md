# 💬 Java Chat Application with GUI & MySQL Integration

This is a **Java-based chat application** with a graphical user interface (GUI), supporting **multi-user communication**, **private/public messaging**, and **MySQL-based persistence**. It includes **robust error handling**, **event processing**, and **modular code structure** for maintainability and scalability.

---

## 📁 Project Structure

```
.
├── ClientGUI.java            # GUI and logic for chat clients
├── ServerGUI.java            # Server-side GUI and connection management
├── JdbcConnectivity.java     # MySQL database connectivity utility
├── database.sql              # SQL schema for user & message storage
└── README.md                 # Project overview and setup instructions
```

---

## ✅ Core Features

- 🔐 **User Authentication**
  - Login/registration with username constraints and secure password hashing (bcrypt).
  - Tracks user activity and online status.

- 💬 **Messaging System**
  - Supports both public chat and private (1:1) messages.
  - Real-time delivery status for messages.

- 🌐 **Client-Server Architecture**
  - Server listens for multiple clients simultaneously.
  - Socket-based communication with efficient data flow.

- 🛠️ **Database Integration**
  - MySQL-backed data persistence with well-structured tables.
  - Messages and user credentials are stored and validated via SQL queries.

---

## ⚙️ Setup Instructions

### 🔧 Prerequisites

- Java JDK 8 or above
- MySQL Server
- JDBC Driver (Connector/J)

### 🏁 Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/java-chat-app.git
   cd java-chat-app
   ```

2. **Configure the MySQL database**

   - Import the schema from `database.sql` into your MySQL instance:
     ```bash
     mysql -u root -p < database.sql
     ```

   - Update credentials in `JdbcConnectivity.java`:
     ```java
     String url = "jdbc:mysql://localhost:3306/chat_application";
     String username = "your_mysql_username";
     String password = "your_mysql_password";
     ```

3. **Compile and Run**

   - Start the **server**:
     ```bash
     javac ServerGUI.java JdbcConnectivity.java
     java ServerGUI
     ```

   - Launch the **client(s)**:
     ```bash
     javac ClientGUI.java JdbcConnectivity.java
     java ClientGUI
     ```

---

## 🧠 Key Concepts & Architecture

### 🔄 Event Handling

- Listeners are efficiently managed via `ActionListener`, `WindowAdapter`, and `KeyListener` to capture user actions.
- Event delegation ensures only relevant UI components update or trigger processes.

### 🧪 Data Validation

- **Client-side**: Username formats and message lengths validated via regex and boundary checks.
- **Server-side**: All SQL operations use prepared statements to prevent SQL injection.

### 🧱 Component Integration

- The UI logic in `ClientGUI` and `ServerGUI` is tightly coupled with backend validation through `JdbcConnectivity`.
- Threads manage multiple clients, and synchronization ensures consistent UI updates.

---

## 🛡️ Error Handling & Robustness

- Graceful handling of:
  - Invalid login attempts
  - Database connection issues
  - Socket disconnections
- Ensures system continuity without crashing.

---

## ✨ Code Quality & Innovation

- Modular classes with **separation of concerns** (UI, DB, logic).
- Well-documented code with descriptive comments.
- Leverages **regex constraints** on usernames and **password salting/hashing** for secure credential management.
- Indexes in the DB enhance query performance for large-scale message tracking.

---

## 📚 Documentation

### 📌 Class Overview

| File               | Description |
|--------------------|-------------|
| `ClientGUI.java`   | Manages the client's UI, login/registration forms, and message panel. |
| `ServerGUI.java`   | Handles server socket, incoming client connections, and server-side GUI. |
| `JdbcConnectivity.java` | Manages all database transactions, including user login, registration, and message logs. |
| `database.sql`     | Defines the schema for user accounts and messages with constraints, indexes, and relationships. |

### 📎 Usage Notes

- Login with valid credentials or register a new user.
- Type messages in the text field to send. Use dropdown to select recipient for private chats.
- The server must be running before clients can connect.

---

## 🧪 Future Improvements

- ✅ Token-based authentication
- 🌍 WebSocket upgrade for better scalability
- 📱 Android or Web interface
- 📊 Admin dashboard with user/message analytics

---

## 👨‍💻 Contributing

Pull requests are welcome! For major changes, please open an issue first to discuss what you'd like to change.

---

## 📄 License

MIT License. See `LICENSE` file for more details.
