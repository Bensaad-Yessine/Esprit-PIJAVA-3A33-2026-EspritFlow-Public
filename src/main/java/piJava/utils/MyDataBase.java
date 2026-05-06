package piJava.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/pidev";
    private static final String DEFAULT_PASSWORD = "";

    private Connection connection;
    private String lastErrorMessage;
    static  MyDataBase instance ;

    // constructeur
     private MyDataBase(){
         reconnect();
     }
     public static MyDataBase getInstance(){
         if(instance==null){
             instance = new MyDataBase();
         }
         return instance ;
     }
    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                reconnect();
            }
        } catch (SQLException e) {
            lastErrorMessage = e.getMessage();
            connection = null;
        }
        return connection;
    }

    public synchronized String getLastErrorMessage() {
        return lastErrorMessage;
    }

    private void reconnect() {
        try {
            String url = EnvConfig.get("DB_URL", DEFAULT_URL);
            String username = EnvConfig.get("DB_USER", DEFAULT_USERNAME);
            String password = EnvConfig.get("DB_PASSWORD", DEFAULT_PASSWORD);

            connection = DriverManager.getConnection(url, username, password);
            lastErrorMessage = null;
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            connection = null;
            lastErrorMessage = e.getMessage();
            System.err.println("Database connection unavailable: " + e.getMessage());
        }
    }
}
