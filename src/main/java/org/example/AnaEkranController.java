package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

// PostgreSQL için gerekli SQL kütüphaneleri
import java.sql.*;

public class AnaEkranController {

    // Neon Bulut PostgreSQL Bağlantı Bilgileri
    private final String JDBC_URL = "jdbc:postgresql://ep-dark-fog-aljbcm61-pooler.c-3.eu-central-1.aws.neon.tech/neondb?sslmode=require";
    private final String USER = "neondb_owner";
    private final String PASSWORD = "npg_6qi9dCQKRzgr";

    @FXML private AnchorPane pnlKitapEkle, pnlKitapListele;
    @FXML private TextField txtKitapAdi, txtYazar, txtSayfaSayisi, txtKiminElinde;
    @FXML private ComboBox<String> cbDurum;
    @FXML private Button btnKaydet;

    @FXML private TableView<Kitap> tabloKitaplar;
    @FXML private TableColumn<Kitap, String> colKitapAdi, colYazar, colDurum;
    @FXML private TextField txtKitapAra;

    private ObservableList<Kitap> kitapListesi = FXCollections.observableArrayList();
    private Kitap duzenlenecekKitap = null;

    @FXML
    public void initialize() {
        if (cbDurum != null) {
            cbDurum.getItems().addAll("Kütüphanede", "Emanette", "Okunuyor", "Kayıp");
            cbDurum.setValue("Kütüphanede");
        }

        colKitapAdi.setCellValueFactory(new PropertyValueFactory<>("kitapAdi"));
        colYazar.setCellValueFactory(new PropertyValueFactory<>("yazar"));
        colDurum.setCellValueFactory(new PropertyValueFactory<>("durum"));

        FilteredList<Kitap> filtrelenmisVeri = new FilteredList<>(kitapListesi, p -> true);
        txtKitapAra.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrelenmisVeri.setPredicate(kitap -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String kucukHarfFiltre = newValue.toLowerCase();
                return kitap.getKitapAdi().toLowerCase().contains(kucukHarfFiltre) ||
                        kitap.getYazar().toLowerCase().contains(kucukHarfFiltre);
            });
        });

        SortedList<Kitap> siraliVeri = new SortedList<>(filtrelenmisVeri);
        siraliVeri.comparatorProperty().bind(tabloKitaplar.comparatorProperty());
        tabloKitaplar.setItems(siraliVeri);

        pnlKitapEkle.setVisible(true);
        pnlKitapListele.setVisible(false);

        tabloyuVerilerleDoldur();

        pnlKitapEkle.setVisible(true);
        pnlKitapListele.setVisible(false);

    }

    @FXML
    public void sayfaEkleGoster(ActionEvent event) {
        duzenlenecekKitap = null;
        btnKaydet.setText("Kitabı Kaydet");
        txtKitapAdi.clear();
        txtYazar.clear();
        txtKiminElinde.clear();
        pnlKitapEkle.setVisible(true);
        pnlKitapListele.setVisible(false);
    }

    @FXML
    public void sayfaListeGoster(ActionEvent event) {
        pnlKitapEkle.setVisible(false);
        pnlKitapListele.setVisible(true);
        tabloyuVerilerleDoldur();
    }

    @FXML
    public void kitapEkleButonunaTiklandi(ActionEvent event) {
        String kitapAdi = txtKitapAdi.getText();
        String yazar = txtYazar.getText();
        String durum = cbDurum.getValue();
        String kiminElinde = txtKiminElinde.getText();

        if (kitapAdi.isEmpty() || yazar.isEmpty()) {
            mesajGoster("Uyarı", "Boş alan bırakmayın!", Alert.AlertType.WARNING);
            return;
        }

        String sql;
        if (duzenlenecekKitap == null) {
            // Ekleme sorgusu
            sql = "INSERT INTO kitaplar (kitap_adi, yazar, durum, kimin_elinde) VALUES (?, ?, ?, ?)";
        } else {
            // Güncelleme sorgusu
            sql = "UPDATE kitaplar SET kitap_adi=?, yazar=?, durum=?, kimin_elinde=? WHERE kitap_adi=?";
        }

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, kitapAdi);
            pstmt.setString(2, yazar);
            pstmt.setString(3, durum);
            pstmt.setString(4, durum.equals("Emanette") ? kiminElinde : "-");

            if (duzenlenecekKitap != null) {
                pstmt.setString(5, duzenlenecekKitap.getKitapAdi());
            }

            pstmt.executeUpdate();
            mesajGoster("Başarılı", duzenlenecekKitap == null ? "Kitap başarıyla eklendi!" : "Kitap başarıyla güncellendi!", Alert.AlertType.INFORMATION);

            duzenlenecekKitap = null;
            btnKaydet.setText("Kitabı Kaydet");
            txtKitapAdi.clear(); txtYazar.clear(); txtKiminElinde.clear();

        } catch (SQLException e) {
            mesajGoster("Veritabanı Hatası", "Hata oluştu: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void kitapSilButonunaTiklandi(ActionEvent event) {
        Kitap seciliKitap = tabloKitaplar.getSelectionModel().getSelectedItem();
        if (seciliKitap == null) return;

        if (new Alert(Alert.AlertType.CONFIRMATION, "Silmek istediğinize emin misiniz?").showAndWait().get() == ButtonType.OK) {
            String sql = "DELETE FROM kitaplar WHERE kitap_adi = ?";
            try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, seciliKitap.getKitapAdi());
                pstmt.executeUpdate();
                tabloyuVerilerleDoldur(); // Tabloyu yenile

            } catch (SQLException e) {
                mesajGoster("Hata", "Silme işlemi başarısız: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void tabloyuVerilerleDoldur() {
        kitapListesi.clear();
        String sql = "SELECT * FROM kitaplar";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                kitapListesi.add(new Kitap(
                        rs.getString("kitap_adi"),
                        rs.getString("yazar"),
                        rs.getString("durum")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Veri çekme hatası: " + e.getMessage());
        }
    }

    @FXML
    public void kitapDuzenleButonunaTiklandi(ActionEvent event) {
        Kitap seciliKitap = tabloKitaplar.getSelectionModel().getSelectedItem();
        if (seciliKitap == null) return;

        duzenlenecekKitap = seciliKitap;
        btnKaydet.setText("Değişiklikleri Güncelle");
        txtKitapAdi.setText(seciliKitap.getKitapAdi());
        txtYazar.setText(seciliKitap.getYazar());
        cbDurum.setValue(seciliKitap.getDurum());

        pnlKitapEkle.setVisible(true);
        pnlKitapListele.setVisible(false);
    }

    private void mesajGoster(String baslik, String icerik, Alert.AlertType tip) {
        Alert alert = new Alert(tip);
        alert.setTitle(baslik); alert.setHeaderText(null); alert.setContentText(icerik);
        alert.showAndWait();
    }
}