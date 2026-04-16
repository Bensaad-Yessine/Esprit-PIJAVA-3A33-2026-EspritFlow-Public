package piJava.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    private static final String USERNAME = "root";
    private static final String URL = "jdbc:mysql://localhost:3306/pidev";
    private static final String PASSWORD = "";

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
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            lastErrorMessage = null;
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            connection = null;
            lastErrorMessage = e.getMessage();
            System.out.println("Database connection unavailable: " + e.getMessage());
        }
    }
}
