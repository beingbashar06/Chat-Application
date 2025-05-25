
// Import necessary libraries for GUI components, event handling, I/O, and networking
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

// Main class for the Chat Client GUI
public class ClientGUI {

    // Server connection details
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12346;

    // Output stream to send messages to server
    private static PrintWriter out;

    // GUI components
    private static JTextArea chatArea;           // Displays the chat messages
    private static JTextField messageField;      // Field to type messages
    private static String username;              // Stores the user's name

    // Placeholder and watermark configurations
    private static final String PLACEHOLDER_TEXT = "Type here...";
    private static final String WATERMARK_TEXT = "Binary Bros";
    private static Color placeholderColor = Color.GRAY;
    private static Color normalColor = Color.BLACK;

    // Entry point of the program
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    // Method to create and display the GUI
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // Text area for chat messages
        chatArea = new JTextArea();
        chatArea.setEditable(false);  // Prevents users from editing chat history
        JScrollPane scrollPane = new JScrollPane(chatArea);

        // Text field for typing messages
        messageField = new JTextField(PLACEHOLDER_TEXT);
        messageField.setForeground(placeholderColor);

        // Placeholder behavior
        messageField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (messageField.getText().equals(PLACEHOLDER_TEXT)) {
                    messageField.setText("");
                    messageField.setForeground(normalColor);
                }
            }

            public void focusLost(FocusEvent e) {
                if (messageField.getText().isEmpty()) {
                    messageField.setForeground(placeholderColor);
                    messageField.setText(PLACEHOLDER_TEXT);
                }
            }
        });

        // Send message on Enter key press
        messageField.addActionListener(e -> {
            String message = messageField.getText();
            if (!message.trim().isEmpty() && !message.equals(PLACEHOLDER_TEXT)) {
                sendMessage(message);
                messageField.setText("");
            }
        });

        // Add watermark label
        JLabel watermark = new JLabel(WATERMARK_TEXT);
        watermark.setHorizontalAlignment(SwingConstants.CENTER);
        watermark.setForeground(new Color(200, 200, 200)); // Light gray color

        // Add components to frame
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(messageField, BorderLayout.SOUTH);
        frame.add(watermark, BorderLayout.NORTH);

        // Show the GUI
        frame.setVisible(true);

        // Prompt user for a username
        username = JOptionPane.showInputDialog(frame, "Enter your username:");
        if (username == null || username.trim().isEmpty()) {
            username = "Anonymous";
        }

        // Connect to the chat server
        connectToServer();
    }

    // Method to establish connection to server
    private static void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);

            // Send the username to the server
            out.println(username);

            // Thread to listen for incoming messages
            new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line;
                    while ((line = in.readLine()) != null) {
                        chatArea.append(line + "\n");
                    }
                } catch (IOException e) {
                    chatArea.append("Connection lost.\n");
                }
            }).start();

        } catch (IOException e) {
            chatArea.append("Unable to connect to server.\n");
        }
    }

    // Method to send message to the server
    private static void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}
