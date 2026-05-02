package mock;

import java.util.ArrayList;
import java.util.List;

public class MockBackend {
    // Simulates a database call for the Leaderboard
    public static List<String[]> getDummyLeaderboard() {
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"1", "Trojans2026", "2450"});
        data.add(new String[]{"2", "Connect4Master", "2100"});
        data.add(new String[]{"3", "DanielZ", "1850"});
        data.add(new String[]{"4", "Guest_99", "1200"});
        return data;
    }

    // Simulates an authentication check
    public static boolean checkLogin(String user, String pass) {
        return user.equals("admin") && pass.equals("password123");
    }
}