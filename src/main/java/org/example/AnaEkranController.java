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

    @FXML private AnchorPane pnlUyeYonetimi;
    @FXML private TextField txtUyeAdSoyad, txtUyeTelefon, txtUyeEposta;

    @FXML private TableView<Uye> tabloUyeler;
    @FXML private TableColumn<Uye, String> colUyeAdSoyad, colUyeTelefon, colUyeEposta;
    private ObservableList<Uye> uyeListesi = FXCollections.observableArrayList();
    @FXML private TableColumn<Uye, Integer> colUyeId;

    @FXML
    private void sayfaUyeGoster() {
        pnlKitapEkle.setVisible(false);
        pnlKitapListele.setVisible(false);
        pnlUyeYonetimi.setVisible(true); // Üye panelini açar, diğerlerini gizler
    }

    private Uye duzenlenecekUye = null; // Düzenleme modunu anlamak için

    @FXML
    public void uyeSilButonunaTiklandi() {
        Uye secili = tabloUyeler.getSelectionModel().getSelectedItem();
        if (secili == null) return;
        String sql = "DELETE FROM uyeler WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, secili.getId());
            pstmt.executeUpdate();
            tabloyuUyeVerileriyleDoldur();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    public void uyeDuzenleButonunaTiklandi() {
        duzenlenecekUye = tabloUyeler.getSelectionModel().getSelectedItem();
        if (duzenlenecekUye == null) return;
        txtUyeAdSoyad.setText(duzenlenecekUye.getAdSoyad());
        txtUyeTelefon.setText(duzenlenecekUye.getTelefon());
        txtUyeEposta.setText(duzenlenecekUye.getEposta());
    }

    private void temizleVeYenile() {
        txtUyeAdSoyad.clear(); txtUyeTelefon.clear(); txtUyeEposta.clear();
        duzenlenecekUye = null;
        tabloyuUyeVerileriyleDoldur();
    }

    @FXML
    private void uyeKaydetButonunaTiklandi() {
        String ad = txtUyeAdSoyad.getText();
        String tel = txtUyeTelefon.getText();
        String mail = txtUyeEposta.getText();

        if (ad.isEmpty()) return; // İsim boşsa hiçbir şey yapma

        String sql;
        // Eğer duzenlenecekUye boşsa YENİ KAYIT yap, doluysa GÜNCELLEME yap
        if (duzenlenecekUye == null) {
            sql = "INSERT INTO uyeler (ad_soyad, telefon, eposta) VALUES (?, ?, ?)";
        } else {
            sql = "UPDATE uyeler SET ad_soyad=?, telefon=?, eposta=? WHERE id=?";
        }

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ad);
            pstmt.setString(2, tel);
            pstmt.setString(3, mail);

            // GÜNCELLEME yapılıyorsa, 4. parametre olarak ID'yi gönderiyoruz
            if (duzenlenecekUye != null) {
                pstmt.setInt(4, duzenlenecekUye.getId());
            }

            pstmt.executeUpdate();

            // İşlem bitince her şeyi sıfırla
            duzenlenecekUye = null;
            txtUyeAdSoyad.clear();
            txtUyeTelefon.clear();
            txtUyeEposta.clear();

            tabloyuUyeVerileriyleDoldur(); // Tabloyu tazele!
            System.out.println("Veritabanı başarıyla güncellendi!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ObservableList<Kitap> kitapListesi = FXCollections.observableArrayList();
    private Kitap duzenlenecekKitap = null;

    @FXML
    public void initialize() {
        if (cbDurum != null) {
            cbDurum.getItems().addAll("Kütüphanede", "Emanette", "Okunuyor", "Kayıp");
            cbDurum.setValue("Kütüphanede");
            pnlKitapEkle.setVisible(true);
            pnlKitapListele.setVisible(false);
            pnlUyeYonetimi.setVisible(false); // Bunu ekle!

            tabloyuVerilerleDoldur();
            // Üye Tablosu Sütun Ayarları
            colUyeAdSoyad.setCellValueFactory(new PropertyValueFactory<>("adSoyad"));
            colUyeTelefon.setCellValueFactory(new PropertyValueFactory<>("telefon"));
            colUyeEposta.setCellValueFactory(new PropertyValueFactory<>("eposta"));

            tabloyuUyeVerileriyleDoldur(); // Başlangıçta verileri çek
            colUyeId.setCellValueFactory(new PropertyValueFactory<>("id"));
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
        pnlUyeYonetimi.setVisible(false);
    }

    @FXML
    public void sayfaListeGoster(ActionEvent event) {
        pnlKitapEkle.setVisible(false);
        pnlKitapListele.setVisible(true);
        tabloyuVerilerleDoldur();
        pnlUyeYonetimi.setVisible(false);
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

    private void tabloyuUyeVerileriyleDoldur() {
        uyeListesi.clear();
        String sql = "SELECT * FROM uyeler";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                uyeListesi.add(new Uye(
                        rs.getInt("id"),
                        rs.getString("ad_soyad"),
                        rs.getString("telefon"),
                        rs.getString("eposta")
                ));
            }
            tabloUyeler.setItems(uyeListesi);
        } catch (SQLException e) {
            System.err.println("Üye listesi çekilirken hata: " + e.getMessage());
        }
    }

}