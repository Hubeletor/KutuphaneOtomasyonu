package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Objects;

public class ArayuzApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Giriş ekranını yüklüyoruz
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/GirisEkran.fxml")));

        primaryStage.setTitle("Sistem Girişi");
        Scene scene = new Scene(root, 500, 350);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}