package client;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpClientUtil {

    public static String login(String username, String password) {
        try {
            URL url = new URL("http://localhost:8080/backend/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String params = "username=" + username + "&password=" + password;

            OutputStream os = conn.getOutputStream();
            os.write(params.getBytes());
            os.flush();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            return reader.readLine();

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    public static LeaderboardResponse getLeaderboard(String query, int page, int size) {
        try {
            String safeQuery = (query == null) ? "" : query.trim();
            String urlString = "http://localhost:8080/backend/leaderboard?page=" + page + "&size=" + size + "&q=" + encode(safeQuery);
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            return parseLeaderboard(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return new LeaderboardResponse(List.of(), 0);
        }
    }

    private static LeaderboardResponse parseLeaderboard(String json) {
        if (json == null || json.isBlank()) {
            return new LeaderboardResponse(List.of(), 0);
        }

        int total = 0;
        Matcher totalMatch = Pattern.compile("\\\"total\\\":(\\d+)").matcher(json);
        if (totalMatch.find()) {
            total = Integer.parseInt(totalMatch.group(1));
        }

        List<LeaderboardEntry> entries = new ArrayList<>();
        Matcher rowMatcher = Pattern.compile("\\{\\\"username\\\":\\\"(.*?)\\\",\\\"elo\\\":(\\d+),\\\"inMatch\\\":(true|false)\\}")
                .matcher(json);
        while (rowMatcher.find()) {
            String username = rowMatcher.group(1);
            int elo = Integer.parseInt(rowMatcher.group(2));
            boolean inMatch = Boolean.parseBoolean(rowMatcher.group(3));
            entries.add(new LeaderboardEntry(username, elo, inMatch));
        }

        return new LeaderboardResponse(entries, total);
    }

    private static String encode(String value) throws UnsupportedEncodingException {
        return java.net.URLEncoder.encode(value, "UTF-8");
    }
}