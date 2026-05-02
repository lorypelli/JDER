# JDER

**Java Diagrammi E/R** — Un editor moderno e multipiattaforma per diagrammi Entità-Relazione, Use Case e delle Classi

[![Website](https://img.shields.io/badge/Website-jder.it-blue?style=for-the-badge)](https://jder.it/)
[![GitHub Release](https://img.shields.io/github/v/release/LoryPelli/JDER?style=for-the-badge)](https://github.com/LoryPelli/JDER/releases/latest)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE.md)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-purple?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-orange?style=for-the-badge&logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform)

[🌐 Sito Web](https://jder.it/) • [⬇️ Download](https://github.com/LoryPelli/JDER/releases/download/JDER/JDER.jar) • [🐛 Segnala un problema](https://github.com/LoryPelli/JDER/issues)

---

## Panoramica

JDER è una riscrittura completa in **Kotlin** e **Jetpack Compose** del software originale creato da [Alessandro Ballini](https://ballini.it/Software/ProgER) e successivamente mantenuto da [Gianvito Pio](https://gianvitopio.wordpress.com/jder). Porta con sé un'interfaccia Material 3 moderna e pulita, nuovi tipi di diagramma e un'esperienza utente notevolmente migliorata.

---

## Funzionalità

### 🗂️ Tipi di Diagramma

#### Diagramma Entità-Relazione (E/R)

- Crea e gestisci **Entità** con pieno supporto agli attributi
  - Attributi normali, chiave (chiave primaria), multivalore, derivati e composti
  - Supporto alle entità deboli
- Crea **Relazioni** tra entità con notazione di cardinalità completa
  - Cardinalità supportate: `1`, `(0,1)`, `(1,1)`, `N`, `(0,N)`, `(1,N)`
  - Conversione in entità associativa (relazioni N-a-N)
- Aggiungi **Note testuali** in qualsiasi punto della lavagna
- Pannello Proprietà per modificare elementi e connessioni
- Menu contestuale per azioni rapide su qualsiasi elemento

#### Diagramma delle Classi

- Crea **Classi**, **Interfacce**, **Enum** e **Classi Astratte** (con etichette stereotipo UML)
- Aggiungi **Attributi** e **Metodi** a ogni classe con:
  - Modificatori di visibilità: Pubblico (`+`), Privato (`-`), Protetto (`#`), Package (`~`)
  - Flag per membri statici e astratti
  - Annotazione del tipo per ogni membro
- Disegna **Relazioni** tra classi:
  - Associazione, Aggregazione, Composizione, Ereditarietà, Realizzazione, Dipendenza
  - Etichette di molteplicità sorgente e destinazione su ogni relazione
  - Etichetta della relazione opzionale
- Aggiungi **documentazione** inline a qualsiasi classe
- Aggiungi **Note testuali** condivise con il componente note degli altri diagrammi

#### Diagramma dei Casi d'Uso

- Crea **Attori** (con rappresentazione a omino stilizzato)
- Crea **Casi d'Uso** (con rappresentazione a ellisse)
- Disegna **Relazioni** tra elementi:
  - Associazione, Include (`<<include>>`), Extend (`<<extend>>`), Generalizzazione
  - Frecce correttamente renderizzate con direzionalità appropriata
- Definisci **Confini di Sistema** per raggruppare i casi d'uso
- Aggiungi **Note testuali** condivise con il componente note E/R

---

### 🎨 Interfaccia & UX

- Sistema di design **Material 3** in tutta l'applicazione
- **Modalità Chiara & Scura** con transizione animata fluida
- **20 palette di colori Material ufficiali** selezionabili a runtime:
  - Rosso, Rosa, Viola, Viola Scuro, Indaco, Blu, Blu Chiaro, Ciano, Verde Acqua, Verde, Verde Chiaro, Lime, Giallo, Ambra, Arancione, Arancione Scuro, Marrone, Grigio, Grigio Blu, Bianco & Nero
- **Navigazione a schede** tra i diagrammi E/R e Use Case
- **Pannello Proprietà** — barra laterale contestuale per modificare gli elementi selezionati
- **Menu Contestuale** — clic destro su qualsiasi elemento della lavagna per azioni rapide

---

### 🖱️ Controlli della Lavagna

| Azione                  | Controllo                              |
| ----------------------- | -------------------------------------- |
| Zoom Avanti             | `Ctrl` + `+` o scroll mouse su        |
| Zoom Indietro           | `Ctrl` + `-` o scroll mouse giù       |
| Scorrimento             | Trascinamento con tasto centrale mouse |
| Seleziona e Sposta      | Clic sinistro + trascinamento          |
| Apri menu contestuale   | Clic destro sull'elemento              |
| Reimposta Zoom          | Menu Vista → Reimposta Zoom            |

---

### ⌨️ Scorciatoie da Tastiera

| Scorciatoia        | Azione                          |
| ------------------ | ------------------------------- |
| `Ctrl + Z`         | Annulla                         |
| `Ctrl + Y`         | Ripeti                          |
| `Ctrl + S`         | Salva                           |
| `Ctrl + Shift + S` | Salva con nome                  |
| `Ctrl + O`         | Apri                            |
| `Ctrl + N`         | Nuovo Diagramma                 |
| `Ctrl + +`         | Zoom Avanti                     |
| `Ctrl + -`         | Zoom Indietro                   |
| `Delete`           | Elimina elemento selezionato    |

---

### 💾 Salvataggio & Esportazione

- Salva i diagrammi in formato **JSON** (`.json`)
- Apri e ricarica diagrammi salvati in precedenza
- Esporta qualsiasi diagramma come **immagine PNG**
- Finestra di dialogo per modifiche non salvate alla chiusura o all'apertura di un nuovo diagramma
- Cronologia globale annulla/ripeti (fino a 50 stati)

---

## Stack Tecnologico

| Tecnologia                                                                   | Utilizzo                   |
| ---------------------------------------------------------------------------- | -------------------------- |
| [Kotlin](https://kotlinlang.org/)                                            | Linguaggio principale      |
| [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) | Framework UI               |
| [Material 3](https://m3.material.io/)                                        | Sistema di design          |
| [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)     | Salvataggio/caricamento JSON |
| [Gradle](https://gradle.org/)                                                | Sistema di build           |
| [Cloudflare Workers](https://workers.cloudflare.com/)                        | Hosting del sito web       |
| [Skia](https://skia.org/)                                                    | Rendering/esportazione PNG |
| Java 8                                                                       | Target JVM                 |

---

## Requisiti

- **Java 8** o versione superiore
- Windows, macOS (Intel o Apple Silicon), o Linux (x64 o ARM)

---

## Per Iniziare

### Download

Scarica l'ultimo `JDER.jar` dalla [pagina delle Release](https://github.com/LoryPelli/JDER/releases/latest) oppure direttamente:

```
https://github.com/LoryPelli/JDER/releases/download/JDER/JDER.jar
```

Poi eseguilo con:

```bash
java -jar JDER.jar
```

### Compilare dal sorgente

```bash
git clone https://github.com/LoryPelli/JDER.git
cd JDER
./gradlew run
```

Per creare un JAR standalone:

```bash
./gradlew jar
```

Il file di output si troverà in `build/libs/JDER.jar`.

---

## Struttura del Progetto

```
src/
└── main/
    ├── kotlin/com/jder/
    │   ├── Main.kt                    # Punto di ingresso dell'app
    │   ├── data/
    │   │   ├── DiagramRepository.kt        # Persistenza JSON E/R
    │   │   ├── UseCaseRepository.kt        # Persistenza JSON Use Case
    │   │   ├── ClassDiagramRepository.kt   # Persistenza JSON Diagramma Classi
    │   │   ├── ImageExporter.kt            # Esportazione PNG via Skia
    │   │   └── JsonConfig.kt               # Configurazione kotlinx.serialization
    │   ├── domain/model/
    │   │   ├── ERModels.kt                 # Data class Entità, Relazione, Attributo, Nota
    │   │   ├── UseCaseModels.kt            # Data class Attore, CasoDUso, ConfineDelSistema
    │   │   ├── ClassDiagramModels.kt       # Data class ClassEntity, ClassMember, ClassRelation
    │   │   ├── DiagramState.kt             # Stato E/R + annulla/ripeti
    │   │   ├── UseCaseState.kt             # Stato Use Case + annulla/ripeti
    │   │   ├── ClassDiagramState.kt        # Stato Diagramma Classi + annulla/ripeti
    │   │   ├── ToolMode.kt                 # Modalità strumento E/R
    │   │   ├── UseCaseToolMode.kt          # Modalità strumento Use Case
    │   │   └── ClassDiagramToolMode.kt     # Modalità strumento Diagramma Classi
    │   └── ui/
    │       ├── components/            # Componenti UI riutilizzabili
    │       ├── dialogs/               # Finestre di dialogo per proprietà e creazione
    │       ├── screens/               # AppScreen, MainScreen, UseCaseScreen, ClassDiagramScreen
    │       ├── theme/                 # Tema, Palette Colori, StatoTema
    │       └── utils/                 # Renderer diagrammi e PNG
    └── resources/
        └── jder_icon.png
```

---

## Credits

JDER is a complete rewrite inspired by the work of:

- **Alessandro Ballini** — [Original JDER](https://ballini.it/Software/ProgER)
- **Gianvito Pio** — [Maintained version](https://gianvitopio.wordpress.com/jder)

---

## License

This project is licensed under the **MIT License** — see [LICENSE.md](LICENSE.md) for details.

---

Made with ❤️ by [LoryPelli](https://github.com/LoryPelli) • [jder.it](https://jder.it/)
