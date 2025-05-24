import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerGUI {
    private static final int PORT = 12346;
    private static CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static JTextArea logArea;
    private static JTextField messageField;
    private static JList<String> clientList;
    private static DefaultListModel<String> clientListModel;
    private static final String WATERMARK_TEXT = "Made by Shamik";
    private static final String PLACEHOLDER_TEXT = "Type server message here...";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
        startServer();
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Chat Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);
        frame.setLayout(new BorderLayout());

        logArea = new JTextArea() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                        RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(150, 150, 150, 100));
                    g2d.setFont(new Font("Arial", Font.ITALIC, 24));
                    int x = (getWidth() - g2d.getFontMetrics().stringWidth(WATERMARK_TEXT)) / 2;
                    int y = getHeight() / 2;
                    g2d.drawString(WATERMARK_TEXT, x, y);
                }
            }
        };
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        frame.add(logScrollPane, BorderLayout.CENTER);

        JPanel clientPanel = new JPanel(new BorderLayout());
        clientPanel.setPreferredSize(new Dimension(200, 0));
        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        JScrollPane clientScrollPane = new JScrollPane(clientList);
        clientPanel.add(new JLabel("Connected Clients:"), BorderLayout.NORTH);
        clientPanel.add(clientScrollPane, BorderLayout.CENTER);
        frame.add(clientPanel, BorderLayout.EAST);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messageField.setForeground(Color.GRAY);
        messageField.setText(PLACEHOLDER_TEXT);
        
        messageField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (messageField.getText().equals(PLACEHOLDER_TEXT)) {
                    messageField.setText("");
                    messageField.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (messageField.getText().isEmpty()) {
                    messageField.setForeground(Color.GRAY);
                    messageField.setText(PLACEHOLDER_TEXT);
                }
            }
        });
        
        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendServerMessage();
                }
            }
        });

        JButton sendButton = new JButton("Broadcast");
        sendButton.addActionListener(e -> sendServerMessage());
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        frame.add(messagePanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private static void startServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            log("Server running on port " + PORT);
            
            new Thread(() -> {
                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        ClientHandler clientHandler = new ClientHandler(clientSocket);
                        clients.add(clientHandler);
                        new Thread(clientHandler).start();
                    } catch (IOException e) {
                        log("Server error: " + e.getMessage());
                    }
                }
            }).start();
        } catch (IOException e) {
            log("Failed to start server: " + e.getMessage());
        }
    }

    private static void sendServerMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && !message.equals(PLACEHOLDER_TEXT)) {
            broadcast("[Server]: " + message, null);
            log("[Server]: " + message);
            messageField.setText("");
            messageField.setForeground(Color.GRAY);
            messageField.setText(PLACEHOLDER_TEXT);
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
                    clientListModel.addElement(client.getUsername());
                }
            }
        });
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                log("Client connection error: " + e.getMessage());
            }
        }

        public String getUsername() {
            return username;
        }

        @Override
        public void run() {
            try {
                out.println("Enter your username:");
                username = in.readLine();
                log(username + " connected");
                out.println("Welcome " + username + "!");
                updateClientList();

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    log("[" + username + "]: " + inputLine);
                    broadcast("[" + username + "]: " + inputLine, this);
                }
            } catch (IOException e) {
                log(username + " disconnected");
            } finally {
                try {
                    clients.remove(this);
                    updateClientList();
                    in.close();
                    out.close();
                    clientSocket.close();
                } catch (IOException e) {
                    log("Error closing connection: " + e.getMessage());
                }
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }
}