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

    // DNS (TXT record) hatasını aşmak için kullanılan Standart Bağlantı Adresi
    private final String ATLAS_URI = "mongodb://efeesenel_db_user:XiC6pMuFGQq7qMvc@ac-cjdk7tt-shard-00-00.lag4a4l.mongodb.net:27017,ac-cjdk7tt-shard-00-01.lag4a4l.mongodb.net:27017,ac-cjdk7tt-shard-00-02.lag4a4l.mongodb.net:27017/?ssl=true&replicaSet=atlas-sxm2tq-shard-0&authSource=admin&appName=KutuphaneCluster";

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

        try (MongoClient mongoClient = MongoClients.create(ATLAS_URI)) {
            MongoDatabase database = mongoClient.getDatabase("KutuphaneDB");
            MongoCollection<Document> collection = database.getCollection("Kitaplar");

            if (duzenlenecekKitap == null) {
                Document yeniDoc = new Document("kitapAdi", kitapAdi)
                        .append("yazar", yazar)
                        .append("durum", durum)
                        .append("kiminElinde", durum.equals("Emanette") ? kiminElinde : "-");
                collection.insertOne(yeniDoc);
                mesajGoster("Başarılı", "Kitap Atlas'a başarıyla eklendi!", Alert.AlertType.INFORMATION);
            } else {
                collection.updateOne(
                        new Document("kitapAdi", duzenlenecekKitap.getKitapAdi()),
                        new Document("$set", new Document("kitapAdi", kitapAdi)
                                .append("yazar", yazar)
                                .append("durum", durum)
                                .append("kiminElinde", durum.equals("Emanette") ? kiminElinde : "-"))
                );
                duzenlenecekKitap = null;
                btnKaydet.setText("Kitabı Kaydet");
                mesajGoster("Başarılı", "Kitap başarıyla güncellendi!", Alert.AlertType.INFORMATION);
            }
            txtKitapAdi.clear(); txtYazar.clear(); txtKiminElinde.clear();
        } catch (Exception e) {
            mesajGoster("Bağlantı Hatası", "Atlas sunucusuna ulaşılamadı: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void kitapSilButonunaTiklandi(ActionEvent event) {
        Kitap seciliKitap = tabloKitaplar.getSelectionModel().getSelectedItem();
        if (seciliKitap == null) return;

        if (new Alert(Alert.AlertType.CONFIRMATION, "Silmek istediğinize emin misiniz?").showAndWait().get() == ButtonType.OK) {
            try (MongoClient mongoClient = MongoClients.create(ATLAS_URI)) {
                MongoDatabase database = mongoClient.getDatabase("KutuphaneDB");
                MongoCollection<Document> collection = database.getCollection("Kitaplar");
                collection.deleteOne(new Document("kitapAdi", seciliKitap.getKitapAdi()));
                tabloyuVerilerleDoldur();
            }
        }
    }

    private void tabloyuVerilerleDoldur() {
        kitapListesi.clear();
        try (MongoClient mongoClient = MongoClients.create(ATLAS_URI)) {
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