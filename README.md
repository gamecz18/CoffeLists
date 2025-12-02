# CoffeLists ☕

CoffeLists je mobilní aplikace pro Android, která vám pomůže sledovat a organizovat vaši sbírku káv. Zaznamenejte si detaily o každé kávě, která vám prošla rukama - od stupně pražení až po poznámky o chuti.

## 📱 Funkce

- **Správa káv**: Přidávejte, upravujte a odstraňujte kávy ze své sbírky
- **Detailní informace**: Zaznamenejte si:
  - Název kávy
  - Stupeň pražení (světlé, středně světlé, střední, středně tmavé, tmavé)
  - Stupeň mletí
  - Hmotnost v gramech
  - Výstupní hmotnost
  - Poznámky k chuti a přípravě
- **Fotografie**: Přidávejte fotografie káv pomocí kamery nebo galerie
- **Vyhledávání**: Rychle najděte kávu podle názvu nebo stupně pražení
- **Moderní UI**: Postaveno s Material Design 3 a Jetpack Compose

## 🛠️ Technologie

- **Kotlin** - programovací jazyk
- **Jetpack Compose** - moderní UI toolkit pro Android
- **Material Design 3** - design systém
- **Navigation Compose** - navigace mezi obrazovkami
- **DataStore** - ukládání dat
- **Kotlinx Serialization** - serializace dat
- **Coil** - načítání a zobrazování obrázků
- **Accompanist Permissions** - správa oprávnění

## 📋 Požadavky

- Android Studio Arctic Fox nebo novější
- Android SDK 36
- Minimální Android verze: 7.0 (API 24)
- Cílová Android verze: 14 (API 35)
- Kotlin 1.9.0+

## 🚀 Instalace a spuštění

1. **Klonování repozitáře**
   ```bash
   git clone https://github.com/gamecz18/CoffeLists.git
   cd CoffeLists
   ```

2. **Otevření projektu**
   - Otevřete Android Studio
   - Vyberte "Open an Existing Project"
   - Zvolte složku s klonovaným projektem

3. **Build projektu**
   ```bash
   ./gradlew build
   ```

4. **Spuštění aplikace**
   - Připojte Android zařízení nebo spusťte emulátor
   - Klikněte na tlačítko "Run" v Android Studiu
   - Nebo z příkazové řádky:
   ```bash
   ./gradlew installDebug
   ```

## 📁 Struktura projektu

```
CoffeLists/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/coffelists/
│   │   │   │   ├── MainActivity.kt          # Hlavní aktivita
│   │   │   │   ├── Coffe.kt                 # Datový model kávy
│   │   │   │   ├── AddCoffeeScreen.kt       # Obrazovka pro přidání kávy
│   │   │   │   ├── CoffeeInfoView.kt        # Detail kávy
│   │   │   │   ├── CoffeeFileWork.kt        # Práce se soubory
│   │   │   │   └── ui/theme/                # Téma aplikace
│   │   │   ├── res/                         # Zdroje (ikony, stringy, atd.)
│   │   │   └── AndroidManifest.xml
│   │   ├── androidTest/                     # Instrumentační testy
│   │   └── test/                            # Unit testy
│   └── build.gradle.kts
├── gradle/                                   # Gradle wrapper
├── build.gradle.kts                          # Root build konfigurační soubor
└── settings.gradle.kts                       # Gradle settings
```

## 🎯 Použití

1. **Přidání kávy**: Klikněte na tlačítko "+" v pravém dolním rohu
2. **Vyplnění údajů**: Zadejte název, vyberte stupeň pražení, přidejte poznámky
3. **Přidání fotografie**: Vyfotografujte kávu nebo vyberte obrázek z galerie
4. **Uložení**: Klikněte na "Uložit" pro přidání kávy do seznamu
5. **Detail kávy**: Klikněte na kávu v seznamu pro zobrazení detailů
6. **Úprava**: V detailu kávy klikněte na tlačítko "Upravit"
7. **Vyhledávání**: Použijte ikonu lupy pro vyhledávání podle názvu nebo pražení

## 🔒 Oprávnění

Aplikace vyžaduje následující oprávnění:
- **Kamera**: Pro pořizování fotografií káv
- **Čtení médií**: Pro výběr fotografií z galerie

## 📦 Verze

- **Aktuální verze**: 1.0.4 (versionCode 5)
- **Minimální Android**: 7.0 (API 24)
- **Cílový Android**: 14 (API 35)

## 🤝 Přispívání

Příspěvky jsou vítány! Pokud chcete přispět:

1. Forkněte projekt
2. Vytvořte feature branch (`git checkout -b feature/AmazingFeature`)
3. Commitněte změny (`git commit -m 'Add some AmazingFeature'`)
4. Pushněte do branch (`git push origin feature/AmazingFeature`)
5. Otevřete Pull Request

## 📝 Changelog

### v1.0.4
- Aktuální stabilní verze
- Základní funkce pro správu káv

## 🐛 Známé problémy

Pokud narazíte na nějaký problém, prosím vytvořte issue na GitHubu.

## 👨‍💻 Autor

gamecz18

## 📄 Licence

Tento projekt je open source. Pro více informací o licenci kontaktujte autora.

---

Vytvořeno s ❤️ pro všechny milovníky kávy
