package piJava.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    final String USERNAME = "root";
    final String url = "jdbc:mysql://localhost:3306/pidev" ;
    final String PASSWORD = "";

    Connection connection ;
    static  MyDataBase instance ;

    // constructeur
     private MyDataBase(){
         try{
         connection = DriverManager.getConnection(url,USERNAME,PASSWORD);
             System.out.println("Connected to database successfully");
     } catch (SQLException e) {
             System.out.println(e.getMessage());
         }

     }
     public static MyDataBase getInstance(){
         if(instance==null){
             instance = new MyDataBase();
         }
         return instance ;
     }
    public Connection getConnection() {
        return connection;
    }
}
