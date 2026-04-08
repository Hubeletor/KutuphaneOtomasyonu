package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class AnaEkranController {

    // --- SAYFA PANELLERİ (StackPane içindeki sayfalarımız) ---
    @FXML private AnchorPane pnlKitapEkle;
    @FXML private AnchorPane pnlKitapListele;

    // --- FORM ELEMANLARI (Kitap Ekleme Sayfası) ---
    @FXML private TextField txtKitapAdi;
    @FXML private TextField txtYazar;
    @FXML private TextField txtSayfaSayisi;
    @FXML private TextField txtKiminElinde;
    @FXML private ComboBox<String> cbDurum;

    // --- TABLO ELEMANLARI (Kitap Listeleme Sayfası) ---
    @FXML private TableView<Kitap> tabloKitaplar;
    @FXML private TableColumn<Kitap, String> colKitapAdi;
    @FXML private TableColumn<Kitap, String> colYazar;
    @FXML private TableColumn<Kitap, String> colDurum;

    // Uygulama ilk açıldığında çalışan hazırlık metodu
    @FXML
    public void initialize() {
        // 1. ComboBox Seçeneklerini Doldur
        if (cbDurum != null) {
            cbDurum.getItems().addAll("Kütüphanede", "Emanette", "Okunuyor", "Kayıp");
            cbDurum.setValue("Kütüphanede");
        }

        // 2. Tablo Sütunlarını Kitap Sınıfı ile Eşleştir (Cell Value Factory)
        // Buradaki isimler ("kitapAdi" vb.) Kitap.java sınıfındaki isimlerle birebir aynı olmalı
        colKitapAdi.setCellValueFactory(new PropertyValueFactory<>("kitapAdi"));
        colYazar.setCellValueFactory(new PropertyValueFactory<>("yazar"));
        colDurum.setCellValueFactory(new PropertyValueFactory<>("durum"));

        // 3. Başlangıçta Formu Göster, Tabloyu Gizle
        pnlKitapEkle.setVisible(true);
        pnlKitapListele.setVisible(false);
    }

    // --- MENÜ GEÇİŞLERİ ---

    @FXML
    public void sayfaEkleGoster(ActionEvent event) {
        pnlKitapEkle.setVisible(true);
        pnlKitapListele.setVisible(false);
    }

    @FXML
    public void sayfaListeGoster(ActionEvent event) {
        pnlKitapEkle.setVisible(false);
        pnlKitapListele.setVisible(true);

        // Liste sayfasına her geçildiğinde veritabanından güncel verileri çek
        tabloyuVerilerleDoldur();
    }

    // --- MONGODB İŞLEMLERİ ---

    // 1. VERİ KAYDETME (Kitap Ekle)
    @FXML
    public void kitapEkleButonunaTiklandi(ActionEvent event) {
        String kitapAdi = txtKitapAdi.getText();
        String yazar = txtYazar.getText();
        String durum = cbDurum.getValue();
        String kiminElinde = txtKiminElinde.getText();

        if (kitapAdi.isEmpty() || yazar.isEmpty()) {
            mesajGoster("Uyarı", "Kitap adı ve yazar boş olamaz!", Alert.AlertType.WARNING);
            return;
        }

        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("KutuphaneDB");
            MongoCollection<Document> collection = database.getCollection("Kitaplar");

            Document yeniKitap = new Document("kitapAdi", kitapAdi)
                    .append("yazar", yazar)
                    .append("durum", durum)
                    .append("kiminElinde", durum.equals("Emanette") ? kiminElinde : "-");

            collection.insertOne(yeniKitap);
            mesajGoster("Başarılı", "'" + kitapAdi + "' başarıyla eklendi!", Alert.AlertType.INFORMATION);

            // Temizlik
            txtKitapAdi.clear();
            txtYazar.clear();
            txtSayfaSayisi.clear();
            txtKiminElinde.clear();

        } catch (Exception e) {
            mesajGoster("Hata", "Veritabanına kaydedilemedi: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // 2. VERİ ÇEKME (MongoDB'den Tabloya)
    private void tabloyuVerilerleDoldur() {
        ObservableList<Kitap> kitapListesi = FXCollections.observableArrayList();

        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("KutuphaneDB");
            MongoCollection<Document> collection = database.getCollection("Kitaplar");

            // Tüm dökümanları çek ve Kitap nesnesine çevirerek listeye ekle
            for (Document doc : collection.find()) {
                kitapListesi.add(new Kitap(
                        doc.getString("kitapAdi"),
                        doc.getString("yazar"),
                        doc.getString("durum")
                ));
            }

            // Oluşturduğumuz listeyi tabloya bas
            tabloKitaplar.setItems(kitapListesi);

        } catch (Exception e) {
            System.err.println("Veriler çekilirken hata oluştu: " + e.getMessage());
        }
    }

    // Ortak mesaj kutusu fonksiyonu
    private void mesajGoster(String baslik, String icerik, Alert.AlertType tip) {
        Alert alert = new Alert(tip);
        alert.setTitle(baslik);
        alert.setHeaderText(null);
        alert.setContentText(icerik);
        alert.showAndWait();
    }
}