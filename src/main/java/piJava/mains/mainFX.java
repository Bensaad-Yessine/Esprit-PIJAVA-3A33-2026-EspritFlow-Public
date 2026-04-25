package piJava.mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import piJava.api.StatsApiServer;

public class mainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        try {
            StatsApiServer.getInstance().start();
        } catch (Exception e) {
            System.out.println("Impossible de démarrer l'API statistiques: " + e.getMessage());
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();

    }

    @Override
    public void stop() throws Exception {
        StatsApiServer.getInstance().stop();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
