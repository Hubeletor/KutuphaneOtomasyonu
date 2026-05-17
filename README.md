# 📚 Bulut Tabanlı Kütüphane Yönetim Sistemi

Bu proje, JavaFX ve bulut tabanlı ilişkisel veritabanı (PostgreSQL) mimarisi kullanılarak geliştirilmiş modern bir **Kütüphane Yönetim Sistemi** prototipidir. Uygulama; kitap takibi, üye yönetimi ve emanet süreçlerinin , gerçek zamanlı olarak yönetilmesini sağlar.

## 🛠️ Kullanılan Teknolojiler

* **Dil:** Java (JDK 17+)
* **Arayüz (UI):** JavaFX & FXML
* **Veritabanı:** PostgreSQL (Neon DB Cloud)
* **Bağlantı Yönetimi:** JDBC (Java Database Connectivity)

## Öne Çıkan Özellikler

* **Bulut Veritabanı Entegrasyonu:** Tüm veriler bulut üzerinde (Neon DB) `sslmode=require` güvenliğiyle şifrelenmiş olarak saklanır.
* **Dinamik Filtreleme ve Arama:** `FilteredList` yapısı sayesinde kitap, üye ve emanet tablolarında veritabanını yormadan bellekte anlık (real-time) arama yapılır.
* **Gelişmiş Emanet ve İade Sistemi:** Kitapların ödünç verilmesi ve iade alınması süreçleri ilişkisel veritabanı mantığıyla (Primary Key / ID odaklı) tutarlı bir şekilde yönetilir. Bir kitap emanet verildiğinde veya iade alındığında stok durumu otomatik güncellenir.
* **Güvenli CRUD İşlemleri:** SQL Injection riskine karşı tüm veritabanı sorgularında `PreparedStatement` kullanılmıştır.

## Sistemi Çalıştırma

### Gereksinimler
* Bilgisayarınızda Java 17 veya üzeri bir JDK kurulu olmalıdır.
* JavaFX kütüphanelerinin projenize (Maven/pom.xml üzerinden) entegre olduğundan emin olun.

### Giriş Bilgileri
Uygulama güvenli giriş paneline sahiptir. Sistemi test etmek için aşağıdaki bilgileri kullanabilirsiniz:
* **Kullanıcı Adı:** `admin`
* **Şifre:** `123456`

### Kişi isimleri
Efe Esenel = Hübeletor
İlkcan Tekin = Tkns59
Yusuf Osman Cengiz = Yoscen

