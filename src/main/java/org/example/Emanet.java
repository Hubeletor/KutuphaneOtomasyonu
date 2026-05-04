package org.example;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Emanet {
    private final IntegerProperty id;
    private final StringProperty kitapAdi;
    private final StringProperty uyeAdi;
    private final ObjectProperty<LocalDate> almaTarihi;
    private final ObjectProperty<LocalDate> teslimTarihi;
    private final StringProperty durum;

    public Emanet(int id, String kitapAdi, String uyeAdi, LocalDate almaTarihi, LocalDate teslimTarihi, String durum) {
        this.id = new SimpleIntegerProperty(id);
        this.kitapAdi = new SimpleStringProperty(kitapAdi);
        this.uyeAdi = new SimpleStringProperty(uyeAdi);
        this.almaTarihi = new SimpleObjectProperty<>(almaTarihi);
        this.teslimTarihi = new SimpleObjectProperty<>(teslimTarihi);
        this.durum = new SimpleStringProperty(durum);
    }

    public IntegerProperty idProperty() { return id; }
    public StringProperty kitapAdiProperty() { return kitapAdi; }
    public StringProperty uyeAdiProperty() { return uyeAdi; }
    public ObjectProperty<LocalDate> almaTarihiProperty() { return almaTarihi; }
    public ObjectProperty<LocalDate> teslimTarihiProperty() { return teslimTarihi; }
    public StringProperty durumProperty() { return durum; }

    public int getId() { return id.get(); }
    public String getKitapAdi() { return kitapAdi.get(); }
    public String getUyeAdi() { return uyeAdi.get(); }
    public LocalDate getAlmaTarihi() { return almaTarihi.get(); }
    public LocalDate getTeslimTarihi() { return teslimTarihi.get(); }
    public String getDurum() { return durum.get(); }
}