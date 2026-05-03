package org.example;

import javafx.beans.property.SimpleStringProperty;

public class Kitap {
    private int id;
    private final SimpleStringProperty kitapAdi;
    private final SimpleStringProperty yazar;
    private final SimpleStringProperty durum;

    public Kitap(int id, String kitapAdi, String yazar, String durum) {
        this.id = id;
        this.kitapAdi = new SimpleStringProperty(kitapAdi);
        this.yazar = new SimpleStringProperty(yazar);
        this.durum = new SimpleStringProperty(durum);
    }

    // Tablonun verileri okuyabilmesi için bu "get" metodları şarttır
    public String getKitapAdi() { return kitapAdi.get(); }
    public String getYazar() { return yazar.get(); }
    public String getDurum() { return durum.get(); }
    public int getId() { return id; }
}