---

title: JDER - Editor di Diagrammi ER, Schemi Logici, Use Case, Class, Sequenza, Logici e Macchine a Stati
description: Editor gratuito e open source per diagrammi Entity-Relationship, schemi logici relazionali, Use Case, Class Diagram, diagrammi di sequenza, diagrammi logici e macchine a stati finiti di Mealy e di Moore. Scritto in Kotlin e Compose Multiplatform con interfaccia Material 3, gira su Windows, macOS e Linux.
keywords: JDER, editor diagrammi ER, Entity-Relationship, schema logico, schema relazionale, chiavi esterne, Use Case diagram, Class diagram, diagramma delle classi, Sequence diagram, diagramma di sequenza, diagramma logico, porte logiche, tabella di verità, macchina a stati finiti, macchina di Mealy, macchina di Moore, tabella di transizione, modellazione database, UML, Kotlin, Compose Multiplatform, open source, gratuito, Windows, macOS, Linux
url: https://jder.it/
---

# JDER — Editor di Diagrammi ER, Schemi Logici, Use Case, Class, Sequenza, Logici e Macchine a Stati

**JDER** è un editor gratuito e open source per diagrammi **Entity-Relationship**, **schemi logici relazionali**, **Use Case**, **Class Diagram**, **diagrammi di sequenza**, **diagrammi logici** e **macchine a stati finiti di Mealy e di Moore**, scritto in Kotlin e Compose Multiplatform. Gira su Windows, macOS e Linux con un singolo file JAR.

## Il Tuo Editor di Diagrammi Moderno

Nuova versione del software di [Alessandro Ballini](https://ballini.it/Software/ProgER), poi successivamente ripresa da [Gianvito Pio](https://gianvitopio.wordpress.com/jder), con una veste grafica più moderna e correzione di vari errori, riscritta completamente in Kotlin e Compose.

[Scarica JDER](/download) | [Guida Utente PDF](/guida.pdf)

## Tipi di Diagramma

### Diagramma Entity-Relationship

Crea entità, relazioni e attributi (normali, chiave, multivalore, derivati, composti) con supporto per entità deboli e relazioni N a N con entità associative.

### Schema Logico

Il passo successivo all'E/R nella progettazione di una base di dati: crea tabelle e colonne con chiavi primarie (anche composte), colonne opzionali e a valore unico, e chiavi esterne collegate da frecce che evitano le altre tabelle.

### Diagramma delle Classi

Crea classi, interfacce, enumerazioni e classi astratte con attributi, metodi, visibilità e relazioni UML (associazione, aggregazione, composizione, ereditarietà, realizzazione, dipendenza).

### Diagramma Use Case

Crea attori, casi d'uso, confini di sistema e relazioni (associazione, include, extend, generalizzazione).

### Diagramma di Sequenza

Crea linee di vita (attori, oggetti, boundary, control, entity) e messaggi sincroni, asincroni, di risposta, di creazione e di distruzione, con barre di attivazione automatiche e frammenti combinati (`alt`, `opt`, `loop`, `par`, `break`, `critical`, `ref`).

### Diagramma Logico

Crea circuiti con porte logiche (AND, OR, NOT, NAND, NOR, XOR, XNOR), ingressi e uscite collegati da fili instradati automaticamente, con simulazione in tempo reale, tabella di verità ed espressioni booleane.

### Macchina di Mealy

Crea macchine a stati finiti in cui l'uscita dipende dallo stato e dall'ingresso: stati, stato iniziale e transizioni etichettate `ingresso/uscita` (anche autoanelli), con tabella di transizione e simulazione di una sequenza di ingressi.

### Macchina di Moore

Crea macchine a stati finiti in cui l'uscita dipende solo dallo stato: ogni stato mostra nome e uscita, con controlli su determinismo e transizioni mancanti, tabella di transizione e simulazione passo per passo della sequenza di uscita.

## Caratteristiche Principali

### Interfaccia Moderna

Design Material 3 pulito e intuitivo con supporto tema chiaro/scuro, 20 palette di colori selezionabili e interfaccia in italiano o in inglese.

### Esportazione Multipla

Salva i tuoi diagrammi in formato JSON o esportali come immagine PNG.

### Scorciatoie da Tastiera

Undo/Redo (`Ctrl+Z`/`Ctrl+Y`), Salva (`Ctrl+S`), Apri (`Ctrl+O`), Esporta (`Ctrl+E`), Zoom (`Ctrl+±`) e altro (`Cmd` su macOS).

### Note e Annotazioni

Aggiungi note e commenti testuali a qualsiasi punto del canvas.

### Veloce e Reattivo

Scritto in Kotlin con Compose Multiplatform per massime prestazioni su Windows, macOS e Linux.

## Screenshot

![Diagramma E/R in JDER (tema chiaro): studenti, corsi e docenti di un'università](/screenshots/er-light.png)

![Diagramma E/R in JDER (tema scuro): studenti, corsi e docenti di un'università](/screenshots/er-dark.png)

![Schema Logico in JDER (tema chiaro): tabelle, chiavi primarie e chiavi esterne di un'università](/screenshots/relational-light.png)

![Schema Logico in JDER (tema scuro): tabelle, chiavi primarie e chiavi esterne di un'università](/screenshots/relational-dark.png)

![Diagramma delle Classi in JDER (tema chiaro): e-commerce con ereditarietà, interfacce ed enumerazioni](/screenshots/class-light.png)

![Diagramma delle Classi in JDER (tema scuro): e-commerce con ereditarietà, interfacce ed enumerazioni](/screenshots/class-dark.png)

![Diagramma Use Case in JDER (tema chiaro): sistema di gestione di una biblioteca](/screenshots/usecase-light.png)

![Diagramma Use Case in JDER (tema scuro): sistema di gestione di una biblioteca](/screenshots/usecase-dark.png)

![Diagramma di Sequenza in JDER (tema chiaro): prenotazione di un libro in una biblioteca](/screenshots/sequence-light.png)

![Diagramma di Sequenza in JDER (tema scuro): prenotazione di un libro in una biblioteca](/screenshots/sequence-dark.png)

![Diagramma Logico in JDER (tema chiaro): sommatore completo con porte XOR, AND e OR e valori simulati](/screenshots/logic-light.png)

![Diagramma Logico in JDER (tema scuro): sommatore completo con porte XOR, AND e OR e valori simulati](/screenshots/logic-dark.png)

![Macchina di Mealy in JDER (tema chiaro): riconoscitore della sequenza 101 con le uscite sulle transizioni](/screenshots/mealy-light.png)

![Macchina di Mealy in JDER (tema scuro): riconoscitore della sequenza 101 con le uscite sulle transizioni](/screenshots/mealy-dark.png)

![Macchina di Moore in JDER (tema chiaro): riconoscitore della sequenza 101 con le uscite negli stati](/screenshots/moore-light.png)

![Macchina di Moore in JDER (tema scuro): riconoscitore della sequenza 101 con le uscite negli stati](/screenshots/moore-dark.png)

## Tecnologie

- Kotlin
- Compose Multiplatform
- Material 3
- Java

---

© 2025-2026 LoryPelli — [GitHub Repository](https://github.com/LoryPelli/JDER)
