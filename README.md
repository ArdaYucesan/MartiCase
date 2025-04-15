# Martı Case Study - Gerçek Zamanlı Konum Takip Uygulaması

## Genel Bakış
Bu Android uygulaması, Google Maps ve Routes API kullanarak gerçek zamanlı konum takibi ve rota hesaplama özelliklerini kullanmaktadır. Proje, modern Android geliştirme pratiklerini, Clean Architecture prensiplerini sergilemektedir.

## Özellikler
- Gerçek zamanlı konum takibi
- Özel işaretçi yerleştirme
- Rota hesaplama ve görüntüleme
- Android Service ile uygulama ön plan ve arka planda lokasyon takibi
- Mesafe bazlı yol noktası işaretçileri (her 100m'de bir)
- Adres gösterimi için geocoding

## Teknik Detaylar
- **Mimari:** MVVM + MVI Hybrid ve Clean Architecture
- **Programlama Dili:** Kotlin
- **Bağımlılık Enjeksiyonu:** Koin
- **Ağ İşlemleri:** Ktor Client
- **Harita:** Google Maps SDK
- **Rota API:** Google Routes API
- **Asenkron İşlemler:** Coroutines & Flow
- **Reaktif UI:** LiveData
- **View Binding:** Görünüm erişimi için

## Not
- **Uygulamanın kullanılabilmesi için Google Cloud Api Key gereklidir , Api key olmadığı durumda Routes API network çağrısı normal şekilde hata mesajı gösterir fakat GoogleMap Fragment davranışı için edge case testi yapılmadı**

## Kurulum Notları
- **Projeyi çalıştırmadan önce secret.properties dosyası oluşturmanız gerekmektedir.**
- **secret.properties dosyasına Google Cloud Platform API anahtarınızı (API Key) ve Routes API Base URL bilgisini ekleyin.**
- **Eğer secret.properties dosyası bulunamazsa, proje otomatik olarak secret.default.properties dosyasını kullanacaktır**
- **Uygulamada kullandığım  Routes API Base URL bilgisi, secret.default.properties dosyasında mevcuttur**

## Proje Yapısı
```plaintext
app/
├── data/
│   ├── gps/         # Konum servisleri implementasyonu
│   └── network/     # Ağ katmanı implementasyonu
├── domain/
│   ├── repository/  # Repository arayüzleri
│   ├── use_case/    # İş mantığı
│   └── utils/       # Domain yardımcıları
└── presentation/
    └── map_screen/  # UI bileşenleri
