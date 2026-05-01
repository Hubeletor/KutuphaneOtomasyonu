package org.example;
import javafx.beans.property.*;
public class Uye {

    private final IntegerProperty id;
    private final StringProperty adSoyad;
    private final StringProperty telefon;
    private final StringProperty eposta;

    public Uye(int id, String adSoyad, String telefon, String eposta) {
        this.id = new SimpleIntegerProperty(id);
        this.adSoyad = new SimpleStringProperty(adSoyad);
        this.telefon = new SimpleStringProperty(telefon);
        this.eposta = new SimpleStringProperty(eposta);
    }

    // TableView'ın verileri görebilmesi için gerekli Property metotları
    public IntegerProperty idProperty() { return id; }
    public StringProperty adSoyadProperty() { return adSoyad; }
    public StringProperty telefonProperty() { return telefon; }
    public StringProperty epostaProperty() { return eposta; }

    // Standart Getterlar
    public int getId() { return id.get(); }
    public String getAdSoyad() { return adSoyad.get(); }
    public String getTelefon() { return telefon.get(); }
    public String getEposta() { return eposta.get(); }

}
