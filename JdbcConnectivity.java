import java.sql.*;

public class JdbcConnectivity {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/chat_application";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "abc@123"; 

    public static void main(String[] args) {
        try {
            // Load driver (optional in modern Java)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connected to DB!");

            // Insert a message
            sendMessage(conn, 1, 2, "Hey there!");

            // Fetch messages between 1 and 2
            getMessages(conn, 1, 2);

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(Connection conn, int senderId, int receiverId, String message) throws SQLException {
        String sql = "INSERT INTO messages (sender_id, receiver_id, message) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, senderId);
        stmt.setInt(2, receiverId);
        stmt.setString(3, message);
        stmt.executeUpdate();
        System.out.println("Message sent!");
    }

    public static void getMessages(Connection conn, int user1, int user2) throws SQLException {
        String sql = "SELECT * FROM messages WHERE " +
                     "(sender_id = ? AND receiver_id = ?) OR " +
                     "(sender_id = ? AND receiver_id = ?) " +
                     "ORDER BY sent_at ASC";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, user1);
        stmt.setInt(2, user2);
        stmt.setInt(3, user2);
        stmt.setInt(4, user1);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            int sender = rs.getInt("sender_id");
            String msg = rs.getString("message");
            Timestamp time = rs.getTimestamp("sent_at");
            System.out.println("[" + time + "] User " + sender + ": " + msg);
        }
    }
}