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
        // Tasarım dosyamızı yüklüyoruz
        // Eğer "Location is not set" hatası alırsan buradaki "/AnaEkran.fxml" yazısından / işaretini sil
        Parent kok = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/AnaEkran.fxml")));

        primaryStage.setTitle("Kütüphane Yönetim Sistemi v1.0");
        Scene sahne = new Scene(kok, 800, 600);
        primaryStage.setScene(sahne);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}