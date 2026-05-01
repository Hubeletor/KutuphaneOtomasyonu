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

import java.sql.*;

public class AnaEkranController {

    // Neon Bulut PostgreSQL Bağlantı Bilgileri
    private final String JDBC_URL = "jdbc:postgresql://ep-dark-fog-aljbcm61-pooler.c-3.eu-central-1.aws.neon.tech/neondb?sslmode=require";
    private final String USER = "neondb_owner";
    private final String PASSWORD = "npg_6qi9dCQKRzgr";

    @FXML private AnchorPane pnlKitapEkle, pnlKitapListele, pnlUyeYonetimi;
    @FXML private TextField txtKitapAdi, txtYazar, txtSayfaSayisi, txtKiminElinde;
    @FXML private TextField txtUyeAdSoyad, txtUyeTelefon, txtUyeEposta, txtUyeAra;
    @FXML private ComboBox<String> cbDurum;
    @FXML private Button btnKaydet;

    @FXML private TableView<Kitap> tabloKitaplar;
    @FXML private TableColumn<Kitap, String> colKitapAdi, colYazar, colDurum;
    @FXML private TextField txtKitapAra;

    @FXML private TableView<Uye> tabloUyeler;
    @FXML private TableColumn<Uye, String> colUyeAdSoyad, colUyeTelefon, colUyeEposta;
    @FXML private TableColumn<Uye, Integer> colUyeId;

    private ObservableList<Uye> uyeListesi = FXCollections.observableArrayList();
    private ObservableList<Kitap> kitapListesi = FXCollections.observableArrayList();
    private Uye duzenlenecekUye = null;
    private Kitap duzenlenecekKitap = null;

    @FXML
    public void initialize() {
        // Tablo Sütun Ayarları
        setupTableColumns();

        if (cbDurum != null) {
            cbDurum.getItems().addAll("Kütüphanede", "Emanette", "Okunuyor", "Kayıp");
            cbDurum.setValue("Kütüphanede");
        }

        // Verileri Çek
        tabloyuVerilerleDoldur();
        tabloyuUyeVerileriyleDoldur();

        // Arama Mantıklarını Kur
        setupSearchLogics();

        // İlk sayfa görünümü
        pnlKitapEkle.setVisible(true);
        pnlKitapListele.setVisible(false);
        pnlUyeYonetimi.setVisible(false);
    }

    private void setupTableColumns() {
        colKitapAdi.setCellValueFactory(new PropertyValueFactory<>("kitapAdi"));
        colYazar.setCellValueFactory(new PropertyValueFactory<>("yazar"));
        colDurum.setCellValueFactory(new PropertyValueFactory<>("durum"));

        colUyeId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUyeAdSoyad.setCellValueFactory(new PropertyValueFactory<>("adSoyad"));
        colUyeTelefon.setCellValueFactory(new PropertyValueFactory<>("telefon"));
        colUyeEposta.setCellValueFactory(new PropertyValueFactory<>("eposta"));
    }

    private void setupSearchLogics() {
        // Kitap Arama
        FilteredList<Kitap> filtrelenmisKitaplar = new FilteredList<>(kitapListesi, p -> true);
        txtKitapAra.textProperty().addListener((obs, eski, yeni) -> {
            filtrelenmisKitaplar.setPredicate(kitap -> {
                if (yeni == null || yeni.isEmpty()) return true;
                String filtre = yeni.toLowerCase();
                return kitap.getKitapAdi().toLowerCase().contains(filtre) ||
                        kitap.getYazar().toLowerCase().contains(filtre);
            });
        });
        SortedList<Kitap> siraliKitaplar = new SortedList<>(filtrelenmisKitaplar);
        siraliKitaplar.comparatorProperty().bind(tabloKitaplar.comparatorProperty());
        tabloKitaplar.setItems(siraliKitaplar);

        // Üye Arama
        FilteredList<Uye> filtrelenmisUyeler = new FilteredList<>(uyeListesi, p -> true);
        txtUyeAra.textProperty().addListener((obs, eski, yeni) -> {
            filtrelenmisUyeler.setPredicate(uye -> {
                if (yeni == null || yeni.isEmpty()) return true;
                String filtre = yeni.toLowerCase();
                return uye.getAdSoyad().toLowerCase().contains(filtre) ||
                        uye.getTelefon().contains(filtre);
            });
        });
        SortedList<Uye> siraliUyeler = new SortedList<>(filtrelenmisUyeler);
        siraliUyeler.comparatorProperty().bind(tabloUyeler.comparatorProperty());
        tabloUyeler.setItems(siraliUyeler);
    }

    // --- ÜYE İŞLEMLERİ ---

    @FXML private void sayfaUyeGoster() {
        pnlKitapEkle.setVisible(false); pnlKitapListele.setVisible(false); pnlUyeYonetimi.setVisible(true);
    }

    @FXML private void uyeKaydetButonunaTiklandi() {
        String ad = txtUyeAdSoyad.getText(); String tel = txtUyeTelefon.getText(); String mail = txtUyeEposta.getText();
        if (ad.isEmpty()) return;

        String sql = (duzenlenecekUye == null) ? "INSERT INTO uyeler (ad_soyad, telefon, eposta) VALUES (?, ?, ?)" : "UPDATE uyeler SET ad_soyad=?, telefon=?, eposta=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ad); pstmt.setString(2, tel); pstmt.setString(3, mail);
            if (duzenlenecekUye != null) pstmt.setInt(4, duzenlenecekUye.getId());
            pstmt.executeUpdate();
            temizleVeYenile();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML public void uyeSilButonunaTiklandi() {
        Uye secili = tabloUyeler.getSelectionModel().getSelectedItem();
        if (secili == null) return;
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM uyeler WHERE id = ?")) {
            pstmt.setInt(1, secili.getId());
            pstmt.executeUpdate();
            tabloyuUyeVerileriyleDoldur();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML public void uyeDuzenleButonunaTiklandi() {
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

    private void tabloyuUyeVerileriyleDoldur() {
        uyeListesi.clear();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM uyeler")) {
            while (rs.next()) {
                uyeListesi.add(new Uye(rs.getInt("id"), rs.getString("ad_soyad"), rs.getString("telefon"), rs.getString("eposta")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- KİTAP İŞLEMLERİ ---

    @FXML public void sayfaEkleGoster(ActionEvent event) {
        duzenlenecekKitap = null; btnKaydet.setText("Kitabı Kaydet");
        txtKitapAdi.clear(); txtYazar.clear(); txtKiminElinde.clear();
        pnlKitapEkle.setVisible(true); pnlKitapListele.setVisible(false); pnlUyeYonetimi.setVisible(false);
    }

    @FXML public void sayfaListeGoster(ActionEvent event) {
        pnlKitapEkle.setVisible(false); pnlKitapListele.setVisible(true); pnlUyeYonetimi.setVisible(false);
        tabloyuVerilerleDoldur();
    }

    @FXML public void kitapEkleButonunaTiklandi(ActionEvent event) {
        String kitapAdi = txtKitapAdi.getText(); String yazar = txtYazar.getText(); String durum = cbDurum.getValue();
        if (kitapAdi.isEmpty() || yazar.isEmpty()) return;
        String sql = (duzenlenecekKitap == null) ? "INSERT INTO kitaplar (kitap_adi, yazar, durum) VALUES (?, ?, ?)" : "UPDATE kitaplar SET kitap_adi=?, yazar=?, durum=? WHERE kitap_adi=?";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, kitapAdi); pstmt.setString(2, yazar); pstmt.setString(3, durum);
            if (duzenlenecekKitap != null) pstmt.setString(4, duzenlenecekKitap.getKitapAdi());
            pstmt.executeUpdate();
            duzenlenecekKitap = null; btnKaydet.setText("Kitabı Kaydet");
            txtKitapAdi.clear(); txtYazar.clear();
            tabloyuVerilerleDoldur();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML public void kitapSilButonunaTiklandi(ActionEvent event) {
        Kitap secili = tabloKitaplar.getSelectionModel().getSelectedItem();
        if (secili == null) return;
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM kitaplar WHERE kitap_adi = ?")) {
            pstmt.setString(1, secili.getKitapAdi());
            pstmt.executeUpdate();
            tabloyuVerilerleDoldur();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML public void kitapDuzenleButonunaTiklandi(ActionEvent event) {
        Kitap secili = tabloKitaplar.getSelectionModel().getSelectedItem();
        if (secili == null) return;
        duzenlenecekKitap = secili;
        btnKaydet.setText("Değişiklikleri Güncelle");
        txtKitapAdi.setText(secili.getKitapAdi());
        txtYazar.setText(secili.getYazar());
        cbDurum.setValue(secili.getDurum());
        pnlKitapEkle.setVisible(true); pnlKitapListele.setVisible(false);
    }

    private void tabloyuVerilerleDoldur() {
        kitapListesi.clear();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM kitaplar")) {
            while (rs.next()) {
                kitapListesi.add(new Kitap(rs.getString("kitap_adi"), rs.getString("yazar"), rs.getString("durum")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}