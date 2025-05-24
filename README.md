# Java GUI-Based Client-Server Chat Application

This project is a simple **Client-Server Chat Application** built using Java Swing for the GUI and Java Sockets for network communication. The application allows basic message exchange between a server and multiple clients.

## Features

- Java Swing GUI for both client and server
- Real-time message exchange using sockets
- Console area to view communication logs
- Clean and intuitive user interface

## Files Included

- `ClientGUI.java` – GUI and logic for the client-side application.
- `ServerGUI.java` – GUI and logic for the server-side application.

## Requirements

- Java Development Kit (JDK) 8 or later
- An IDE like IntelliJ IDEA, Eclipse, or simply a terminal to compile and run

## How to Run

### 1. Compile the source files

```bash
javac ServerGUI.java ClientGUI.java
```

### 2. Run the server

```bash
java ServerGUI
```

### 3. Run one or more clients

```bash
java ClientGUI
```

### 4. Start Chatting

- Enter messages in the input field and press send.
- Server and clients can communicate over a localhost connection.

## Notes

- Ensure the server is running before starting the client.
- The default host is `localhost` and port is set inside the code.

## License

This project is open-source and free to use under the MIT License.
