import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.regex.*;

public class ClientGUI {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12346;
    private static PrintWriter out;
    private static JTextArea chatArea;
    private static JTextField messageField;
    private static String username;
    private static JLabel statusLabel;
    private static boolean isConnected = false;
    
    // Constants
    private static final String PLACEHOLDER_TEXT = "Type message here...";
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final int MAX_MESSAGE_LENGTH = 2000;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);
        frame.setLayout(new BorderLayout());

        // Connection panel
        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton connectButton = new JButton("Connect");
        statusLabel = new JLabel("Disconnected");
        statusLabel.setForeground(Color.RED);
        connectionPanel.add(connectButton);
        connectionPanel.add(statusLabel);
        frame.add(connectionPanel, BorderLayout.NORTH);

        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Message panel
        JPanel messagePanel = new JPanel(new BorderLayout());
        messageField = new JTextField(PLACEHOLDER_TEXT);
        messageField.setForeground(Color.GRAY);
        messageField.setEnabled(false);
        
        // Placeholder handling
        messageField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (messageField.getText().equals(PLACEHOLDER_TEXT)) {
                    messageField.setText("");
                    messageField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (messageField.getText().isEmpty()) {
                    messageField.setForeground(Color.GRAY);
                    messageField.setText(PLACEHOLDER_TEXT);
                }
            }
        });

        // Message sending
        messageField.addActionListener(e -> sendMessage());
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        frame.add(messagePanel, BorderLayout.SOUTH);

        // Connect button action
        connectButton.addActionListener(e -> {
            if (!isConnected) {
                promptUsername(frame);
            } else {
                disconnectFromServer();
                connectButton.setText("Connect");
                statusLabel.setText("Disconnected");
                statusLabel.setForeground(Color.RED);
                messageField.setEnabled(false);
            }
        });

        frame.setVisible(true);
    }

    private static void promptUsername(JFrame parent) {
        String input;
        do {
            input = JOptionPane.showInputDialog(parent, 
                "Enter your username (3-20 alphanumeric chars):");
            if (input == null) return;
            
            if (!USERNAME_PATTERN.matcher(input).matches()) {
                JOptionPane.showMessageDialog(parent, 
                    "Invalid username! Must be 3-20 alphanumeric characters.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                input = null;
            }
        } while (input == null);
        
        username = input;
        connectToServer();
    }

    private static void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            isConnected = true;
            
            // Send username to server
            out.println(username);
            
            // Update UI
            statusLabel.setText("Connected as " + username);
            statusLabel.setForeground(new Color(0, 150, 0));
            messageField.setEnabled(true);
            
            // Message listener thread
            new Thread(() -> {
                try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()))) {
                    
                    String line;
                    while ((line = in.readLine()) != null) {
                        final String message = line;
                        SwingUtilities.invokeLater(() -> {
                            chatArea.append(message + "\n");
                            chatArea.setCaretPosition(chatArea.getDocument().getLength());
                        });
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> {
                        chatArea.append("Connection lost: " + e.getMessage() + "\n");
                        statusLabel.setText("Disconnected");
                        statusLabel.setForeground(Color.RED);
                        messageField.setEnabled(false);
                    });
                } finally {
                    isConnected = false;
                }
            }).start();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, 
                "Failed to connect to server: " + e.getMessage(), 
                "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void disconnectFromServer() {
        if (out != null) {
            out.println("/quit");
            isConnected = false;
        }
    }

    private static void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && !message.equals(PLACEHOLDER_TEXT)) {
            if (message.length() > MAX_MESSAGE_LENGTH) {
                JOptionPane.showMessageDialog(null, 
                    "Message too long! Maximum " + MAX_MESSAGE_LENGTH + " characters.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                out.println(message);
                messageField.setText("");
            } catch (Exception e) {
                chatArea.append("Failed to send message: " + e.getMessage() + "\n");
            }
        }
    }
}