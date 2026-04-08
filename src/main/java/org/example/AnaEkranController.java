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
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class AnaEkranController {

    // --- SAYFA PANELLERİ ---
    @FXML private AnchorPane pnlKitapEkle, pnlKitapListele;

    // --- FORM ELEMANLARI ---
    @FXML private TextField txtKitapAdi, txtYazar, txtSayfaSayisi, txtKiminElinde;
    @FXML private ComboBox<String> cbDurum;
    @FXML private Button btnKaydet; // Yeni eklediğimiz buton tanımı

    // --- TABLO VE ARAMA ELEMANLARI ---
    @FXML private TableView<Kitap> tabloKitaplar;
    @FXML private TableColumn<Kitap, String> colKitapAdi, colYazar, colDurum;
    @FXML private TextField txtKitapAra;

    // --- VERİ LİSTESİ VE HAFIZA ---
    private ObservableList<Kitap> kitapListesi = FXCollections.observableArrayList();
    private Kitap duzenlenecekKitap = null;

    @FXML
    public void initialize() {
        // 1. ComboBox Seçenekleri
        if (cbDurum != null) {
            cbDurum.getItems().addAll("Kütüphanede", "Emanette", "Okunuyor", "Kayıp");
            cbDurum.setValue("Kütüphanede");
        }

        // 2. Tablo Sütun Bağlantıları
        colKitapAdi.setCellValueFactory(new PropertyValueFactory<>("kitapAdi"));
        colYazar.setCellValueFactory(new PropertyValueFactory<>("yazar"));
        colDurum.setCellValueFactory(new PropertyValueFactory<>("durum"));

        // 3. Canlı Arama Filtresi
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

        // Başlangıç görünümü
        pnlKitapEkle.setVisible(true);
        pnlKitapListele.setVisible(false);
    }

    // --- SAYFA GEÇİŞLERİ ---
    @FXML
    public void sayfaEkleGoster(ActionEvent event) {
        duzenlenecekKitap = null; // Yeni kayıt moduna geç
        btnKaydet.setText("Kitabı Kaydet"); // Buton metnini sıfırla

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

    // --- VERİTABANI İŞLEMLERİ ---
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

            if (duzenlenecekKitap == null) {
                // MOD: YENİ EKLEME
                Document yeniDoc = new Document("kitapAdi", kitapAdi)
                        .append("yazar", yazar)
                        .append("durum", durum)
                        .append("kiminElinde", durum.equals("Emanette") ? kiminElinde : "-");

                collection.insertOne(yeniDoc);
                mesajGoster("Başarılı", "Yeni kitap eklendi.", Alert.AlertType.INFORMATION);
            } else {
                // MOD: GÜNCELLEME
                collection.updateOne(
                        new Document("kitapAdi", duzenlenecekKitap.getKitapAdi()),
                        new Document("$set", new Document("kitapAdi", kitapAdi)
                                .append("yazar", yazar)
                                .append("durum", durum)
                                .append("kiminElinde", durum.equals("Emanette") ? kiminElinde : "-"))
                );

                mesajGoster("Başarılı", "Kitap bilgileri güncellendi.", Alert.AlertType.INFORMATION);
                duzenlenecekKitap = null; // İşlem bitince hafızayı sıfırla
                btnKaydet.setText("Kitabı Kaydet"); // Butonu eski haline getir
            }

            // Formu temizle
            txtKitapAdi.clear();
            txtYazar.clear();
            txtKiminElinde.clear();

        } catch (Exception e) {
            mesajGoster("Hata", "İşlem başarısız: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void kitapSilButonunaTiklandi(ActionEvent event) {
        Kitap seciliKitap = tabloKitaplar.getSelectionModel().getSelectedItem();
        if (seciliKitap == null) {
            mesajGoster("Uyarı", "Lütfen tablodan bir kitap seçin!", Alert.AlertType.WARNING);
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(null);
        alert.setContentText(seciliKitap.getKitapAdi() + " silinsin mi?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
                MongoDatabase database = mongoClient.getDatabase("KutuphaneDB");
                MongoCollection<Document> collection = database.getCollection("Kitaplar");
                collection.deleteOne(new Document("kitapAdi", seciliKitap.getKitapAdi()));
                tabloyuVerilerleDoldur();
                mesajGoster("Başarılı", "Kitap silindi.", Alert.AlertType.INFORMATION);
            }
        }
    }

    private void tabloyuVerilerleDoldur() {
        kitapListesi.clear();
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("KutuphaneDB");
            MongoCollection<Document> collection = database.getCollection("Kitaplar");
            for (Document doc : collection.find()) {
                kitapListesi.add(new Kitap(doc.getString("kitapAdi"), doc.getString("yazar"), doc.getString("durum")));
            }
        } catch (Exception e) {
            System.err.println("Veri çekme hatası: " + e.getMessage());
        }
    }

    @FXML
    public void kitapDuzenleButonunaTiklandi(ActionEvent event) {
        Kitap seciliKitap = tabloKitaplar.getSelectionModel().getSelectedItem();
        if (seciliKitap == null) {
            mesajGoster("Uyarı", "Lütfen düzenlenecek kitabı seçin!", Alert.AlertType.WARNING);
            return;
        }

        duzenlenecekKitap = seciliKitap;
        btnKaydet.setText("Değişiklikleri Güncelle"); // Düzenleme modunda buton metni değişir

        txtKitapAdi.setText(seciliKitap.getKitapAdi());
        txtYazar.setText(seciliKitap.getYazar());
        cbDurum.setValue(seciliKitap.getDurum());

        pnlKitapEkle.setVisible(true);
        pnlKitapListele.setVisible(false);
    }

    private void mesajGoster(String baslik, String icerik, Alert.AlertType tip) {
        Alert alert = new Alert(tip);
        alert.setTitle(baslik);
        alert.setHeaderText(null);
        alert.setContentText(icerik);
        alert.showAndWait();
    }
}