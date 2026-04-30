package piJava.mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class mainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        // Start the Spring Boot REST API in the background
        Thread apiThread = new Thread(() -> {
            try {
                org.springframework.boot.SpringApplication.run(piJava.ChatApiApplication.class, args);
            } catch (Throwable e) {
                try {
                    java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter("spring_error.txt"));
                    e.printStackTrace(pw);
                    pw.close();
                } catch(Exception ex) {}
                System.err.println("Failed to start Spring Boot API: " + e.getMessage());
                e.printStackTrace();
            }
        });
        apiThread.setDaemon(true);
        apiThread.start();

        launch(args);
    }
}
