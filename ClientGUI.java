import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ClientGUI {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12346;
    private static PrintWriter out;
    private static JTextArea chatArea;
    private static JTextField messageField;
    private static String username;
    private static final String PLACEHOLDER_TEXT = "Type here...";
    private static final String WATERMARK_TEXT = "Binary Bros";
    private static Color placeholderColor = Color.GRAY;
    private static Color normalColor = Color.BLACK;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        chatArea = new JTextArea() {
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
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        frame.add(chatScrollPane, BorderLayout.CENTER);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messageField.setForeground(placeholderColor);
        messageField.setText(PLACEHOLDER_TEXT);
        
        messageField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (messageField.getText().equals(PLACEHOLDER_TEXT)) {
                    messageField.setText("");
                    messageField.setForeground(normalColor);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (messageField.getText().isEmpty()) {
                    messageField.setForeground(placeholderColor);
                    messageField.setText(PLACEHOLDER_TEXT);
                }
            }
        });
        
        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        frame.add(messagePanel, BorderLayout.SOUTH);

        new Thread(() -> connectToServer()).start();

        frame.setVisible(true);
    }

    private static void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String serverResponse = in.readLine();
            if (serverResponse.equals("Enter your username:")) {
                username = JOptionPane.showInputDialog("Enter your username:");
                if (username == null || username.trim().isEmpty()) {
                    username = "Anonymous" + (int)(Math.random() * 1000);
                }
                out.println(username);
                appendToChat("Connected to the chat server as " + username);
            }

            appendToChat(in.readLine());

            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        appendToChat(message);
                    }
                } catch (IOException e) {
                    appendToChat("Disconnected from server: " + e.getMessage());
                }
            }).start();

        } catch (IOException e) {
            appendToChat("Failed to connect to server: " + e.getMessage());
        }
    }

    private static void sendMessage() {
        String message = messageField.getText().trim();
        // Don't send if it's empty or the placeholder text
        if (!message.isEmpty() && !message.equals(PLACEHOLDER_TEXT)) {
            out.println(message);
            messageField.setText("");
            messageField.setForeground(placeholderColor);
            messageField.setText(PLACEHOLDER_TEXT);
            messageField.requestFocus();
        }
    }

    private static void appendToChat(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
}