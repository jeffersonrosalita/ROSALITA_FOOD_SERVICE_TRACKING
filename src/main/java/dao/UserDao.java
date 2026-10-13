package dao;

import Model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserDao {

    public boolean register(String name, String password, String role)
            throws SQLException {

        String sql = """
                INSERT INTO "User" (name, password, role, status)
                VALUES (?, ?, ?, 'PENDING')
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name.trim());
            ps.setString(2, password);
            ps.setString(3, role);

            return ps.executeUpdate() == 1;
        }
    }

    public Optional<User> login(String name, String password)
            throws SQLException {

        String sql = """
                SELECT id, name, password, role, status
                FROM "User"
                WHERE name = ? AND password = ?
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name.trim());
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("status")
                    ));
                }

                return Optional.empty();
            }
        }
    }
}