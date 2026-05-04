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
import java.time.LocalDate;

public class AnaEkranController {

    private final String JDBC_URL = "jdbc:postgresql://ep-dark-fog-aljbcm61-pooler.c-3.eu-central-1.aws.neon.tech/neondb?sslmode=require";
    private final String USER = "neondb_owner";
    private final String PASSWORD = "npg_6qi9dCQKRzgr";

    @FXML private AnchorPane pnlKitapEkle, pnlKitapListele, pnlUyeYonetimi, pnlEmanetIslemleri;
    @FXML private TextField txtKitapAdi, txtYazar, txtSayfaSayisi, txtKiminElinde;
    @FXML private TextField txtUyeAdSoyad, txtUyeTelefon, txtUyeEposta, txtUyeAra, txtKitapAra;
    @FXML private ComboBox<String> cbDurum;
    @FXML private Button btnKaydet;

    @FXML private TableView<Kitap> tabloKitaplar;
    @FXML private TableColumn<Kitap, String> colKitapAdi, colYazar, colDurum;
    @FXML private TableColumn<Kitap, Integer> colKitapId;

    @FXML private TableView<Uye> tabloUyeler;
    @FXML private TableColumn<Uye, String> colUyeAdSoyad, colUyeTelefon, colUyeEposta;
    @FXML private TableColumn<Uye, Integer> colUyeId;

    // Emanet Sistemi Elemanları
    @FXML private TextField txtEmanetKitapAra, txtEmanetUyeAra;
    @FXML private ComboBox<String> cbEmanetKitap, cbEmanetUye;
    @FXML private DatePicker dpTeslimTarihi;
    @FXML private TableView<Emanet> tabloEmanetler;
    @FXML private TableColumn<Emanet, Integer> colEmanetId;
    @FXML private TableColumn<Emanet, String> colEmanetKitapAdi, colEmanetUyeAdi, colEmanetDurum;
    @FXML private TableColumn<Emanet, LocalDate> colEmanetAlma, colEmanetTeslim;

    private ObservableList<Uye> uyeListesi = FXCollections.observableArrayList();
    private ObservableList<Kitap> kitapListesi = FXCollections.observableArrayList();
    private ObservableList<Emanet> emanetListesi = FXCollections.observableArrayList();
    private Uye duzenlenecekUye = null;
    private Kitap duzenlenecekKitap = null;

    @FXML
    public void initialize() {
        setupTableColumns();

        if (cbDurum != null) {
            cbDurum.getItems().addAll("Kütüphanede", "Emanette", "Okunuyor", "Kayıp");
            cbDurum.setValue("Kütüphanede");
        }

        tabloyuVerilerleDoldur();
        tabloyuUyeVerileriyleDoldur();
        setupSearchLogics();

        pnlKitapEkle.setVisible(true);
        pnlKitapListele.setVisible(false);
        pnlUyeYonetimi.setVisible(false);
        pnlEmanetIslemleri.setVisible(false);
    }

    private void setupTableColumns() {
        colKitapId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colKitapAdi.setCellValueFactory(new PropertyValueFactory<>("kitapAdi"));
        colYazar.setCellValueFactory(new PropertyValueFactory<>("yazar"));
        colDurum.setCellValueFactory(new PropertyValueFactory<>("durum"));

        colUyeId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUyeAdSoyad.setCellValueFactory(new PropertyValueFactory<>("adSoyad"));
        colUyeTelefon.setCellValueFactory(new PropertyValueFactory<>("telefon"));
        colUyeEposta.setCellValueFactory(new PropertyValueFactory<>("eposta"));

        colEmanetId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmanetKitapAdi.setCellValueFactory(new PropertyValueFactory<>("kitapAdi"));
        colEmanetUyeAdi.setCellValueFactory(new PropertyValueFactory<>("uyeAdi"));
        colEmanetAlma.setCellValueFactory(new PropertyValueFactory<>("almaTarihi"));
        colEmanetTeslim.setCellValueFactory(new PropertyValueFactory<>("teslimTarihi"));
        colEmanetDurum.setCellValueFactory(new PropertyValueFactory<>("durum"));
    }

    private void setupSearchLogics() {
        // Kitap ve Üye Ana Sayfa Aramaları
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

        FilteredList<Uye> filtrelenmisUyeler = new FilteredList<>(uyeListesi, p -> true);
        txtUyeAra.textProperty().addListener((obs, eski, yeni) -> {
            filtrelenmisUyeler.setPredicate(uye -> {
                if (yeni == null || yeni.isEmpty()) return true;
                String filtre = yeni.toLowerCase();
                return uye.getAdSoyad().toLowerCase().contains(filtre) || uye.getTelefon().contains(filtre);
            });
        });
        SortedList<Uye> siraliUyeler = new SortedList<>(filtrelenmisUyeler);
        siraliUyeler.comparatorProperty().bind(tabloUyeler.comparatorProperty());
        tabloUyeler.setItems(siraliUyeler);

        // --- YENİ: EMANET SİSTEMİ DİNAMİK ARAMA MANTIĞI ---
        txtEmanetKitapAra.textProperty().addListener((obs, eski, yeni) -> doldurEmanetKitapComboBox(yeni));
        txtEmanetUyeAra.textProperty().addListener((obs, eski, yeni) -> doldurEmanetUyeComboBox(yeni));
    }

    // --- EMANET İŞLEMLERİ ---

    @FXML
    private void sayfaEmanetGoster() {
        pnlKitapEkle.setVisible(false); pnlKitapListele.setVisible(false); pnlUyeYonetimi.setVisible(false);
        pnlEmanetIslemleri.setVisible(true);

        emanetFormunuGuncelle();
        tabloyuEmanetVerileriyleDoldur();
    }

    private void emanetFormunuGuncelle() {
        txtEmanetKitapAra.clear();
        txtEmanetUyeAra.clear();
        doldurEmanetKitapComboBox("");
        doldurEmanetUyeComboBox("");
        dpTeslimTarihi.setValue(null);
    }

    // Aranan kelimeye göre Kitap ComboBox'ını doldurur
    private void doldurEmanetKitapComboBox(String filtre) {
        cbEmanetKitap.getItems().clear();
        for (Kitap k : kitapListesi) {
            if ("Kütüphanede".equals(k.getDurum())) {
                if (filtre == null || filtre.isEmpty() || k.getKitapAdi().toLowerCase().contains(filtre.toLowerCase())) {
                    cbEmanetKitap.getItems().add(k.getKitapAdi());
                }
            }
        }
    }

    // Aranan kelimeye göre Üye ComboBox'ını doldurur
    private void doldurEmanetUyeComboBox(String filtre) {
        cbEmanetUye.getItems().clear();
        for (Uye u : uyeListesi) {
            if (filtre == null || filtre.isEmpty() || u.getAdSoyad().toLowerCase().contains(filtre.toLowerCase())) {
                cbEmanetUye.getItems().add(u.getAdSoyad());
            }
        }
    }

    @FXML
    private void emanetVerButonunaTiklandi() {
        String secilenKitap = cbEmanetKitap.getValue();
        String secilenUye = cbEmanetUye.getValue();
        LocalDate iadeTarihi = dpTeslimTarihi.getValue();

        if (secilenKitap == null || secilenUye == null || iadeTarihi == null) {
            mesajGoster("Uyarı", "Lütfen kitap, üye ve teslim tarihi seçiniz!", Alert.AlertType.WARNING);
            return;
        }

        int kitapId = -1; int uyeId = -1;
        for (Kitap k : kitapListesi) { if(k.getKitapAdi().equals(secilenKitap)) kitapId = k.getId(); }
        for (Uye u : uyeListesi) { if(u.getAdSoyad().equals(secilenUye)) uyeId = u.getId(); }

        String insertSql = "INSERT INTO emanetler (kitap_id, uye_id, alma_tarihi, teslim_tarihi, durum) VALUES (?, ?, CURRENT_DATE, ?, 'Emanette')";
        String updateSql = "UPDATE kitaplar SET durum = 'Emanette' WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD)) {
            try (PreparedStatement pstmt1 = conn.prepareStatement(insertSql);
                 PreparedStatement pstmt2 = conn.prepareStatement(updateSql)) {

                pstmt1.setInt(1, kitapId);
                pstmt1.setInt(2, uyeId);
                pstmt1.setDate(3, java.sql.Date.valueOf(iadeTarihi));
                pstmt1.executeUpdate();

                pstmt2.setInt(1, kitapId);
                pstmt2.executeUpdate();

                tabloyuVerilerleDoldur();
                tabloyuEmanetVerileriyleDoldur();
                emanetFormunuGuncelle();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void tabloyuEmanetVerileriyleDoldur() {
        emanetListesi.clear();
        String sql = "SELECT e.id, k.kitap_adi, u.ad_soyad, e.alma_tarihi, e.teslim_tarihi, e.durum " +
                "FROM emanetler e JOIN kitaplar k ON e.kitap_id = k.id JOIN uyeler u ON e.uye_id = u.id";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                emanetListesi.add(new Emanet(
                        rs.getInt("id"), rs.getString("kitap_adi"), rs.getString("ad_soyad"),
                        rs.getDate("alma_tarihi") != null ? rs.getDate("alma_tarihi").toLocalDate() : null,
                        rs.getDate("teslim_tarihi") != null ? rs.getDate("teslim_tarihi").toLocalDate() : null,
                        rs.getString("durum")
                ));
            }
            tabloEmanetler.setItems(emanetListesi);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- ESKİ METOTLAR (DEĞİŞTİRİLMEDİ) ---

    @FXML private void sayfaUyeGoster() {
        pnlKitapEkle.setVisible(false); pnlKitapListele.setVisible(false); pnlEmanetIslemleri.setVisible(false); pnlUyeYonetimi.setVisible(true);
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
            temizleVeYenileUye();
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

    private void temizleVeYenileUye() {
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

    @FXML public void sayfaEkleGoster(ActionEvent event) {
        duzenlenecekKitap = null; btnKaydet.setText("Kitabı Kaydet");
        txtKitapAdi.clear(); txtYazar.clear(); txtKiminElinde.clear();
        pnlKitapEkle.setVisible(true); pnlKitapListele.setVisible(false); pnlUyeYonetimi.setVisible(false); pnlEmanetIslemleri.setVisible(false);
    }

    @FXML public void sayfaListeGoster(ActionEvent event) {
        pnlKitapEkle.setVisible(false); pnlKitapListele.setVisible(true); pnlUyeYonetimi.setVisible(false); pnlEmanetIslemleri.setVisible(false);
        tabloyuVerilerleDoldur();
    }

    @FXML public void kitapEkleButonunaTiklandi(ActionEvent event) {
        String kitapAdi = txtKitapAdi.getText(); String yazar = txtYazar.getText(); String durum = cbDurum.getValue();
        if (kitapAdi.isEmpty() || yazar.isEmpty()) return;
        String sql = (duzenlenecekKitap == null) ? "INSERT INTO kitaplar (kitap_adi, yazar, durum) VALUES (?, ?, ?)" : "UPDATE kitaplar SET kitap_adi=?, yazar=?, durum=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, kitapAdi); pstmt.setString(2, yazar); pstmt.setString(3, durum);
            if (duzenlenecekKitap != null) pstmt.setInt(4, duzenlenecekKitap.getId());
            pstmt.executeUpdate();
            duzenlenecekKitap = null; btnKaydet.setText("Kitabı Kaydet");
            txtKitapAdi.clear(); txtYazar.clear();
            tabloyuVerilerleDoldur();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML public void kitapSilButonunaTiklandi(ActionEvent event) {
        Kitap seciliKitap = tabloKitaplar.getSelectionModel().getSelectedItem();
        if (seciliKitap == null) return;
        if (new Alert(Alert.AlertType.CONFIRMATION, "Bu kitabı silmek istediğinize emin misiniz?").showAndWait().get() == ButtonType.OK) {
            String sql = "DELETE FROM kitaplar WHERE id = ?";
            try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, seciliKitap.getId());
                pstmt.executeUpdate();
                tabloyuVerilerleDoldur();
            } catch (SQLException e) {
                mesajGoster("Hata", "Silme işlemi başarısız: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML public void kitapDuzenleButonunaTiklandi(ActionEvent event) {
        Kitap secili = tabloKitaplar.getSelectionModel().getSelectedItem();
        if (secili == null) return;
        duzenlenecekKitap = secili;
        btnKaydet.setText("Değişiklikleri Güncelle");
        txtKitapAdi.setText(secili.getKitapAdi());
        txtYazar.setText(secili.getYazar());
        cbDurum.setValue(secili.getDurum());
        pnlKitapEkle.setVisible(true); pnlKitapListele.setVisible(false); pnlEmanetIslemleri.setVisible(false);
    }

    private void tabloyuVerilerleDoldur() {
        kitapListesi.clear();
        String sql = "SELECT * FROM kitaplar";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                kitapListesi.add(new Kitap(rs.getInt("id"), rs.getString("kitap_adi"), rs.getString("yazar"), rs.getString("durum")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void mesajGoster(String baslik, String icerik, Alert.AlertType tip) {
        Alert alert = new Alert(tip); alert.setTitle(baslik); alert.setHeaderText(null); alert.setContentText(icerik); alert.showAndWait();
    }
}