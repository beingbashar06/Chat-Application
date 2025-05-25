/*
 * JdbcConnectivity.java - Database operations for Chat Application
 * 
 * Handles all MySQL database interactions including:
 * - Establishing connections
 * - Sending messages
 * - Retrieving message history
 * 
 * Uses JDBC with PreparedStatements for security
 */

import java.sql.*;

public class JdbcConnectivity {
    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/chat_application";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "abc@123"; 
    
    // JDBC driver class name (for older Java versions)
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    public static void main(String[] args) {
        try {
            // Load JDBC driver (optional for modern Java versions)
            Class.forName(JDBC_DRIVER);
            
            // Establish database connection
            System.out.println("Connecting to database...");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            
            // Demonstrate functionality
            System.out.println("Connected to DB successfully!");
            
            // Test message insertion
            sendMessage(conn, 1, 2, "Hey there!");
            
            // Retrieve conversation between users 1 and 2
            System.out.println("\nFetching message history...");
            getMessages(conn, 1, 2);
            
            // Clean up
            conn.close();
            System.out.println("Database connection closed.");
            
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Inserts a new message into the database
     * 
     * @param conn Active database connection
     * @param senderId ID of the user sending the message
     * @param receiverId ID of the recipient (use 0 for group messages)
     * @param message The message content
     * @throws SQLException If database operation fails
     */
    public static void sendMessage(Connection conn, int senderId, int receiverId, String message) 
            throws SQLException {
        // Parameterized SQL to prevent SQL injection
        String sql = "INSERT INTO messages (sender_id, receiver_id, message) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Set parameters
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setString(3, message);
            
            // Execute update
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Message sent! Rows affected: " + rowsAffected);
        }
    }

    /**
     * Retrieves conversation history between two users
     * 
     * @param conn Active database connection
     * @param user1 First user ID
     * @param user2 Second user ID
     * @throws SQLException If database operation fails
     */
    public static void getMessages(Connection conn, int user1, int user2) throws SQLException {
        // Query gets messages in both directions
        String sql = "SELECT * FROM messages WHERE " +
                   "(sender_id = ? AND receiver_id = ?) OR " +
                   "(sender_id = ? AND receiver_id = ?) " +
                   "ORDER BY sent_at ASC";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Set parameters for both conversation directions
            stmt.setInt(1, user1);
            stmt.setInt(2, user2);
            stmt.setInt(3, user2);
            stmt.setInt(4, user1);
            
            // Execute query and process results
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\n--- Conversation History ---");
                while (rs.next()) {
                    int sender = rs.getInt("sender_id");
                    String msg = rs.getString("message");
                    Timestamp time = rs.getTimestamp("sent_at");
                    
                    // Format output
                    System.out.printf("[%s] User %d: %s%n", 
                        time.toString(), sender, msg);
                }
                System.out.println("--- End of History ---\n");
            }
        }
    }
}
