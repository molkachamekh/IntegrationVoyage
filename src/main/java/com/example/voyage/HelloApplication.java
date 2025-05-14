package com.example.voyage;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try {
            // For testing, you might want to load a specific screen
            // In a production app, you'd check session and load login or dashboard
            // accordingly
            FXMLLoader fxmlLoader = new FXMLLoader(
                    HelloApplication.class.getResource("/com/example/voyage/user/frontoffice/login.fxml"));

            // If you want to test the client dashboard directly:
            // FXMLLoader fxmlLoader = new
            // FXMLLoader(HelloApplication.class.getResource("/com/example/voyage/user/frontoffice/login.fxml"));

            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);

            // Add CSS
            scene.getStylesheets().add(getClass().getResource("/com/example/voyage/css/styles.css").toExternalForm());

            stage.setTitle("Voyage Application");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}