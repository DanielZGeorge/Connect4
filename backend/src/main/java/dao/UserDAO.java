package dao;

import model.User;
import util.DBConnection;
import util.PasswordUtil;

import java.sql.*;

public class UserDAO {

    public boolean usernameExists(String username) throws Exception {
        String sql = "SELECT user_id FROM Users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            return rs.next();
        }
    }

    public boolean emailExists(String email) throws Exception {
        String sql = "SELECT user_id FROM Users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            return rs.next();
        }
    }

    public boolean createUser(String username, String email, String password) throws Exception {
        if (usernameExists(username) || emailExists(email)) {
            return false;
        }

        String passwordHash = PasswordUtil.hashPassword(password);

        String sql = "INSERT INTO Users (username, email, password_hash, elo_rating) VALUES (?, ?, ?, 1000)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, passwordHash);

            return ps.executeUpdate() == 1;
        }
    }

    public User validateLogin(String username, String password) throws Exception {
        String sql = "SELECT * FROM Users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return null;

            String storedHash = rs.getString("password_hash");

            if (!PasswordUtil.checkPassword(password, storedHash)) {
                return null;
            }

            return new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getInt("elo_rating")
            );
        }
    }

    public void updateElo(int userId, int newElo) throws Exception {
        String sql = "UPDATE Users SET elo_rating = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, newElo);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }
}