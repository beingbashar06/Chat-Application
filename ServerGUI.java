import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.regex.*;

public class ServerGUI {
    private static final int PORT = 12346;
    private static final int MAX_USERNAME_LENGTH = 20;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    
    private static CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static ServerSocket serverSocket;
    private static boolean isRunning = false;
    
    // GUI Components
    private static JTextArea logArea;
    private static JTextField messageField;
    private static JList<String> clientList;
    private static DefaultListModel<String> clientListModel;
    private static JButton startStopButton;
    private static JLabel statusLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Chat Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        // Server control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        startStopButton = new JButton("Start Server");
        statusLabel = new JLabel("Server Stopped");
        statusLabel.setForeground(Color.RED);
        controlPanel.add(startStopButton);
        controlPanel.add(statusLabel);
        frame.add(controlPanel, BorderLayout.NORTH);

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());

        // Log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        contentPanel.add(logScrollPane, BorderLayout.CENTER);

        // Client list
        JPanel clientPanel = new JPanel(new BorderLayout());
        clientPanel.setPreferredSize(new Dimension(200, 0));
        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        JScrollPane clientScrollPane = new JScrollPane(clientList);
        clientPanel.add(new JLabel("Connected Clients:"), BorderLayout.NORTH);
        clientPanel.add(clientScrollPane, BorderLayout.CENTER);
        contentPanel.add(clientPanel, BorderLayout.EAST);

        frame.add(contentPanel, BorderLayout.CENTER);

        // Message panel
        JPanel messagePanel = new JPanel(new BorderLayout());
        messageField = new JTextField("Type server message here...");
        messageField.setForeground(Color.GRAY);
        
        messageField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (messageField.getText().equals("Type server message here...")) {
                    messageField.setText("");
                    messageField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (messageField.getText().isEmpty()) {
                    messageField.setForeground(Color.GRAY);
                    messageField.setText("Type server message here...");
                }
            }
        });

        messageField.addActionListener(e -> sendServerMessage());
        JButton sendButton = new JButton("Broadcast");
        sendButton.addActionListener(e -> sendServerMessage());
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        frame.add(messagePanel, BorderLayout.SOUTH);

        // Server control button action
        startStopButton.addActionListener(e -> {
            if (!isRunning) {
                startServer();
                startStopButton.setText("Stop Server");
                statusLabel.setText("Server Running on Port " + PORT);
                statusLabel.setForeground(new Color(0, 150, 0));
            } else {
                stopServer();
                startStopButton.setText("Start Server");
                statusLabel.setText("Server Stopped");
                statusLabel.setForeground(Color.RED);
            }
        });

        frame.setVisible(true);
    }

    private static void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            log("Server started on port " + PORT);
            
            new Thread(() -> {
                while (isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        ClientHandler clientHandler = new ClientHandler(clientSocket);
                        clients.add(clientHandler);
                        new Thread(clientHandler).start();
                    } catch (IOException e) {
                        if (isRunning) {
                            log("Server error: " + e.getMessage());
                        }
                    }
                }
            }).start();
        } catch (IOException e) {
            log("Failed to start server: " + e.getMessage());
        }
    }

    private static void stopServer() {
        isRunning = false;
        broadcast("[Server]: Server is shutting down...", null);
        
        // Close all client connections
        for (ClientHandler client : clients) {
            try {
                client.sendMessage("[Server]: Server is shutting down");
                client.closeConnection();
            } catch (IOException e) {
                log("Error closing client connection: " + e.getMessage());
            }
        }
        clients.clear();
        updateClientList();
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log("Error closing server socket: " + e.getMessage());
        }
        
        log("Server stopped");
    }

    private static void sendServerMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && !message.equals("Type server message here...")) {
            broadcast("[Server]: " + message, null);
            log("[Server]: " + message);
            messageField.setText("");
        }
    }

    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public static void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void updateClientList() {
        SwingUtilities.invokeLater(() -> {
            clientListModel.clear();
            for (ClientHandler client : clients) {
                if (client.getUsername() != null) {
                    clientListModel.addElement(client.getUsername() + 
                        (client.isAuthenticated() ? " ✓" : ""));
                }
            }
        });
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        private boolean authenticated = false;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                log("Error creating client handler: " + e.getMessage());
            }
        }

        public String getUsername() {
            return username;
        }

        public boolean isAuthenticated() {
            return authenticated;
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void closeConnection() throws IOException {
            out.close();
            in.close();
            clientSocket.close();
        }

        @Override
        public void run() {
            try {
                // Get username
                out.println("Enter your username:");
                String input = in.readLine();
                
                // Validate username
                while (input == null || !USERNAME_PATTERN.matcher(input).matches()) {
                    out.println("Invalid username! Must be 3-20 alphanumeric characters. Try again:");
                    input = in.readLine();
                    if (input == null) break;
                }
                
                if (input != null) {
                    username = input;
                    authenticated = true;
                    log(username + " connected");
                    out.println("Welcome " + username + "! Type /help for commands.");
                    updateClientList();
                    broadcast(username + " has joined the chat", this);

                    // Process messages
                    String message;
                    while ((message = in.readLine()) != null) {
                        if (message.startsWith("/quit")) {
                            break;
                        }
                        log("[" + username + "]: " + message);
                        broadcast("[" + username + "]: " + message, this);
                    }
                }
            } catch (IOException e) {
                log("Connection error with " + username + ": " + e.getMessage());
            } finally {
                try {
                    if (username != null) {
                        broadcast(username + " has left the chat", this);
                        log(username + " disconnected");
                    }
                    clients.remove(this);
                    updateClientList();
                    closeConnection();
                } catch (IOException e) {
                    log("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
}