import java.util.ArrayList;
import java.util.List;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

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

    public static boolean checkLogin(String user, String pass) {
        try {
            URL url = new URL("http://localhost:8080/backend/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String params = "username=" + user + "&password=" + pass;

            OutputStream os = conn.getOutputStream();
            os.write(params.getBytes());
            os.flush();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            String response = reader.readLine();

            System.out.println("Server response: " + response);

            return "LOGIN_SUCCESS".equals(response);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}