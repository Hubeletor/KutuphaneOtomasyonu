package org.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.Objects;

public class GirisController {

    @FXML private TextField txtKullaniciAdi;
    @FXML private PasswordField txtSifre;
    @FXML private Label lblMesaj;

    @FXML
    private void girisYap() {
        String kAdi = txtKullaniciAdi.getText();
        String sifre = txtSifre.getText();

        // İstediğin bilgiler: admin / 123456
        if (kAdi.equals("admin") && sifre.equals("123456")) {
            anaPaneliAc();
        } else {
            lblMesaj.setText("Hatalı kullanıcı adı veya şifre!");
        }
    }

    private void anaPaneliAc() {
        try {
            // Mevcut giriş penceresini kapat
            Stage currentStage = (Stage) txtKullaniciAdi.getScene().getWindow();
            currentStage.close();

            // Ana kütüphane panelini aç
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/AnaEkran.fxml")));
            Stage stage = new Stage();
            stage.setTitle("Kütüphane Yönetim Sistemi v1.0");
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}