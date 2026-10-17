# JDER

**Java Diagrammi E/R** — Un editor moderno e multipiattaforma per diagrammi Entità-Relazione, Use Case, delle Classi e Logici

[![Website](https://img.shields.io/badge/Website-jder.it-blue?style=for-the-badge)](https://jder.it/)
[![GitHub Release](https://img.shields.io/github/v/release/LoryPelli/JDER?style=for-the-badge)](https://github.com/LoryPelli/JDER/releases/latest)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE.md)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-purple?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-orange?style=for-the-badge&logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform)

[🌐 Sito Web](https://jder.it/) • [⬇️ Download](https://jder.it/download) • [🐛 Segnala un problema](https://github.com/LoryPelli/JDER/issues)

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

#### Diagramma Logico

- Inserisci **Porte Logiche** con i simboli standard: `AND`, `OR`, `NOT`, `NAND`, `NOR`, `XOR`, `XNOR`
  - Da 2 a 8 ingressi per le porte a più ingressi, etichetta opzionale (es. `U1`)
- Aggiungi **Ingressi** (interruttori 0/1, cliccabili) e **Uscite** (indicatori luminosi)
- Collega gli elementi **trascinando da un pin all'altro** (oppure con lo strumento Collegamento)
  - I collegamenti vengono instradati automaticamente evitando gli altri componenti
- **Simulazione in tempo reale**: i fili si colorano in base al valore (1, 0 o indefinito)
  - Gestisce anche circuiti con retroazione (es. latch SR) e segnala quelli che oscillano
- **Tabella di verità** generata automaticamente (copiabile negli appunti)
- **Espressione booleana** di ogni uscita e porta nel pannello Proprietà
- Aggiungi **Note testuali** come negli altri diagrammi

---

### 🎨 Interfaccia & UX

- Sistema di design **Material 3** in tutta l'applicazione
- **Modalità Chiara & Scura** con transizione animata fluida
- **20 palette di colori Material ufficiali** selezionabili a runtime:
  - Rosso, Rosa, Viola, Viola Scuro, Indaco, Blu, Blu Chiaro, Ciano, Verde Acqua, Verde, Verde Chiaro, Lime, Giallo, Ambra, Arancione, Arancione Scuro, Marrone, Grigio, Grigio Blu, Bianco & Nero
- **Navigazione a schede** tra i quattro tipi di diagramma (le schede con modifiche non salvate sono segnate con `•`)
- **Pannello Proprietà** — barra laterale contestuale per modificare gli elementi selezionati
- **Menu Contestuale** — clic destro su qualsiasi elemento della lavagna per azioni rapide
- **Suggerimenti** sui pulsanti della barra degli strumenti e scala corretta sugli schermi ad alta densità (Retina/HiDPI)

---

### 🖱️ Controlli della Lavagna

| Azione                  | Controllo                                                    |
| ----------------------- | ------------------------------------------------------------ |
| Zoom Avanti             | `Ctrl` + `+` o scroll mouse su (verso il cursore)            |
| Zoom Indietro           | `Ctrl` + `-` o scroll mouse giù                              |
| Scorrimento             | Trascinamento su un'area vuota o con il tasto centrale       |
| Seleziona e Sposta      | Clic sinistro + trascinamento                                |
| Modifica elemento       | Doppio clic sull'elemento                                    |
| Apri menu contestuale   | Clic destro sull'elemento (`Ctrl` + clic su macOS)           |
| Collega porte logiche   | Trascinamento da un pin a un altro                           |
| Reimposta Zoom          | `Ctrl` + `0` o Menu Vista → Reimposta Zoom                   |

---

### ⌨️ Scorciatoie da Tastiera

Su macOS si può usare `Cmd` al posto di `Ctrl`.

| Scorciatoia                      | Azione                                  |
| -------------------------------- | --------------------------------------- |
| `Ctrl + Z`                       | Annulla                                 |
| `Ctrl + Y` / `Ctrl + Shift + Z`  | Ripeti                                  |
| `Ctrl + S`                       | Salva                                   |
| `Ctrl + Shift + S`               | Salva con nome                          |
| `Ctrl + O`                       | Apri                                    |
| `Ctrl + N`                       | Nuovo Diagramma                         |
| `Ctrl + E`                       | Esporta come PNG                        |
| `Ctrl + +`                       | Zoom Avanti                             |
| `Ctrl + -`                       | Zoom Indietro                           |
| `Ctrl + 0`                       | Reimposta Zoom                          |
| `Delete` / `Backspace`           | Elimina elemento selezionato            |
| `Esc`                            | Deseleziona e torna allo strumento Seleziona |

---

### 💾 Salvataggio & Esportazione

- Salva i diagrammi in formato **JSON** (`.json`)
- Apri e ricarica diagrammi salvati in precedenza (un file di un altro tipo di diagramma viene riconosciuto e non sovrascritto per errore)
- Esporta qualsiasi diagramma come **immagine PNG** ad alta risoluzione, identica a quanto mostrato sulla lavagna e ritagliata sul contenuto
- Finestra di dialogo per modifiche non salvate alla chiusura o all'apertura di un nuovo diagramma
- Cronologia annulla/ripeti per ogni diagramma (fino a 50 stati)

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
| Java 11                                                                      | Target JVM                 |

---

## Requisiti

- **Java 11** o versione superiore
- Windows, macOS (Intel o Apple Silicon), o Linux (x64 o ARM)

---

## Per Iniziare

### Download

Scarica l'ultimo `JDER.jar` dalla [pagina delle Release](https://github.com/LoryPelli/JDER/releases/latest) oppure direttamente:

```
https://jder.it/download
```

Poi eseguilo con:

```bash
java -jar JDER.jar
```

### Diagrammi di esempio

La cartella [`examples/`](examples/) contiene un diagramma per ogni tipo (E/R, classi, casi d'uso e logico), apribili da JDER con **File → Apri Diagramma**. Gli screenshot del sito sono generati da questi file.

### Compilare dal sorgente

```bash
git clone https://github.com/LoryPelli/JDER.git
cd JDER
./gradlew run
```

Per eseguire i test:

```bash
./gradlew test
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
├── test/kotlin/com/jder/            # Test (simulazione logica, file, gesti sulla lavagna)
└── main/
    ├── kotlin/com/jder/
    │   ├── Main.kt                    # Punto di ingresso dell'app
    │   ├── data/
    │   │   ├── DiagramFileRepository.kt    # Persistenza JSON generica con riconoscimento del tipo
    │   │   ├── DiagramRepository.kt        # Persistenza JSON E/R
    │   │   ├── UseCaseRepository.kt        # Persistenza JSON Use Case
    │   │   ├── ClassDiagramRepository.kt   # Persistenza JSON Diagramma Classi
    │   │   ├── LogicDiagramRepository.kt   # Persistenza JSON Diagramma Logico
    │   │   ├── ImageExporter.kt            # Esportazione PNG via Skia
    │   │   └── JsonConfig.kt               # Configurazione kotlinx.serialization
    │   ├── domain/model/
    │   │   ├── DiagramEditorState.kt       # Stato comune: file, modifiche, annulla/ripeti, zoom
    │   │   ├── UndoHistory.kt              # Cronologia annulla/ripeti
    │   │   ├── DiagramType.kt              # Tipi di diagramma (schede e file)
    │   │   ├── ERModels.kt                 # Data class Entità, Relazione, Attributo, Nota
    │   │   ├── UseCaseModels.kt            # Data class Attore, CasoDUso, ConfineDelSistema
    │   │   ├── ClassDiagramModels.kt       # Data class ClassEntity, ClassMember, ClassRelation
    │   │   ├── LogicModels.kt              # Data class LogicComponent, LogicWire, LogicDiagram
    │   │   ├── LogicSimulator.kt           # Simulazione, tabella di verità, espressioni booleane
    │   │   ├── DiagramState.kt             # Stato E/R
    │   │   ├── UseCaseState.kt             # Stato Use Case
    │   │   ├── ClassDiagramState.kt        # Stato Diagramma Classi
    │   │   ├── LogicDiagramState.kt        # Stato Diagramma Logico
    │   │   └── *ToolMode.kt                # Modalità strumento di ogni diagramma
    │   └── ui/
    │       ├── components/            # Lavagne, disegno, gesti, barre strumenti, pannelli
    │       ├── dialogs/               # Finestre di dialogo per proprietà e creazione
    │       ├── screens/               # AppScreen, DiagramEditorScaffold e una schermata per diagramma
    │       ├── theme/                 # Tema, Palette Colori, StatoTema
    │       └── utils/                 # Esportazione PNG e utilità di piattaforma
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
