import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcConnectivity {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/chat_application";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "abc@123";
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // User authentication
    public static boolean authenticateUser(String username, String password) {
        String sql = "SELECT password_hash, salt FROM users WHERE username = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String salt = rs.getString("salt");
                // In real implementation, hash the input password with salt
                // and compare with storedHash
                return true; // Simplified for example
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
            return false;
        }
    }

    // Message persistence
    public static void persistMessage(int senderId, Integer receiverId, String message, String messageType) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, message, message_type) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, senderId);
            if (receiverId == null) {
                stmt.setNull(2, Types.INTEGER);
            } else {
                stmt.setInt(2, receiverId);
            }
            stmt.setString(3, message);
            stmt.setString(4, messageType);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Message creation failed");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error persisting message: " + e.getMessage());
        }
    }

    // Get message history
    public static List<String> getMessageHistory(int userId1, int userId2) {
        List<String> messages = new ArrayList<>();
        String sql = "SELECT u.username, m.message, m.sent_at " +
                     "FROM messages m JOIN users u ON m.sender_id = u.id " +
                     "WHERE (m.sender_id = ? AND m.receiver_id = ?) OR " +
                     "(m.sender_id = ? AND m.receiver_id = ?) " +
                     "ORDER BY m.sent_at ASC";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId1);
            stmt.setInt(2, userId2);
            stmt.setInt(3, userId2);
            stmt.setInt(4, userId1);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                messages.add(String.format("[%s] %s: %s",
                    rs.getTimestamp("sent_at"),
                    rs.getString("username"),
                    rs.getString("message")));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving messages: " + e.getMessage());
        }
        return messages;
    }

    // Update user status
    public static void updateUserStatus(int userId, boolean isOnline) {
        String sql = "UPDATE users SET is_online = ?, last_login = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBoolean(1, isOnline);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating user status: " + e.getMessage());
        }
    }
}