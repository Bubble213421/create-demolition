# Create: Yıkım (Create: Demolition)

NeoForge 1.21.1 + Create 6.0.x için gerçekçi davranan patlayıcılar modu.

## Kurulum
`build/libs/create-demolition-1.21.1-0.1.0.jar` dosyasını Create ile birlikte `mods` klasörüne at.

Derlemek için: `gradle build`

## Üretim zinciri

| Ürün | Makine | Girdi |
|---|---|---|
| Blaze Tuzu ×2 | Karıştırıcı (ısıtmalı) | Blaze Tozu + Kor Unu + 250mB Su |
| Creeper Özü | Karıştırıcı (ısıtmalı) | 2 Barut + Blaze Tuzu |
| Balçık Jeli ×2 | Karıştırıcı | Balçık Topu + Kil Topu + Bal Peteği |
| ⚠ Uçucu Creeper Özütü | Karıştırıcı (**süper ısıtmalı**) | 2 Creeper Özü + Kızıltaş |
| Patlayıcı Kil ×2 | Karıştırıcı | Uçucu Özüt + 2 Balçık Jeli |
| Patlayıcı Kil Şarjı | Sıkıştırıcı (Compactor) | 3 Patlayıcı Kil |
| Kor Termiti Tozu ×2 | Karıştırıcı (ısıtmalı) | Ezilmiş Ham Demir + Blaze Tuzu + Kor Unu |
| Fünye ×2 | Sıralı Montaj | Bakır Levha → Kızıltaş (Deployer) → Creeper Özü (Deployer) → Pres |
| Dinamit ×2 | Tezgah | Kağıt + Creeper Özü + İp |
| ⚠ Uçucu Fıçı | Tezgah | Fıçı + 4 Uçucu Özüt + 4 Tahta |
| Kor Termiti | Tezgah | 4 Termit Tozu |
| Uzaktan Ateşleyici | Tezgah | Elektron Tüpü + Hassas Mekanizma + 5 Demir |

## Mekanikler
- **Kararsız maddeler (⚠):** Taşırken yanarsan, yüksekten düşersen, oka/patlamaya maruz kalırsan envanterindekiler infilak edebilir. Yere atılıp ateşe/lava değerse patlar.
- **Uçucu Fıçı:** Kızıltaş, ateş, ok, üzerine düşme ve yakın patlamayla patlar. İpeksi Dokunuş olmadan kırmaya çalışırsan %60 ihtimalle patlar.
- **Patlayıcı Kil Şarjı:** Duvara/zemine yapıştırılır. Ateş ve darbeyle patlamaz. Fünye takılınca kızıltaş ya da Uzaktan Ateşleyiciyle patlar. Patlama yapıştığı yüzeyin içine doğru olur.
- **Dinamit:** Fırlatılır, seker, 3.5 sn sonra patlar. Suda fitil yavaş yanar.
- **Kor Termiti:** Patlamaz. Çakmak, ateş topu, ateş ya da kızıltaşla tutuşur ve altındaki blokları eriterek ~12 blok aşağı iner. Obsidyeni eritemez. Suya değerse buhar patlaması olur.
- **Patlama fiziği:** Kapalı alanda patlama güçlenir (en fazla %48). Şok dalgası normal patlama alanının 2 katına ulaşır ve uzaklıkla zayıflar. Siper almak etkisini azaltır. Yakındakiler sersemler, yavaşlar, gözleri kararır. Yakındaki patlayıcılar zincirleme patlar.
- **Uzaktan Ateşleyici:** Shift+Sağ tık ile fünyeli şarjları bağla (en fazla 16, 128 blok menzil). Sağ tık ile hepsini sırayla ateşle.
