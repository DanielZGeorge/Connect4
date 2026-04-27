package util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DBConnection {
    public static Connection getConnection() throws Exception {
        Properties props = new Properties();

        InputStream input = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("config.properties");

        if (input == null) {
            throw new Exception("config.properties was not found on the classpath");
        }

        props.load(input);

        String url = props.getProperty("db.url");
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");

        Class.forName("com.mysql.cj.jdbc.Driver");

        return DriverManager.getConnection(url, username, password);
    }
}