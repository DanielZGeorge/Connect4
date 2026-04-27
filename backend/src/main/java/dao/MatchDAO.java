package dao;

import util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class MatchDAO {

    public void saveMatchResult(int player1Id, int player2Id, String outcome) throws Exception {
        String sql = "INSERT INTO Match_History (player1_id, player2_id, outcome) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, player1Id);
            ps.setInt(2, player2Id);
            ps.setString(3, outcome);

            ps.executeUpdate();
        }
    }
}