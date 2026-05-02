package client;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

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
}