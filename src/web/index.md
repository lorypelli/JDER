---
title: JDER - Editor di Diagrammi ER, Schemi Logici, Use Case, Class, Sequenza e Logici
description: Editor gratuito e open source per diagrammi Entity-Relationship, schemi logici relazionali, Use Case, Class Diagram, diagrammi di sequenza e diagrammi logici. Scritto in Kotlin e Compose Multiplatform con interfaccia Material 3, gira su Windows, macOS e Linux.
keywords: JDER, editor diagrammi ER, Entity-Relationship, schema logico, schema relazionale, chiavi esterne, Use Case diagram, Class diagram, diagramma delle classi, Sequence diagram, diagramma di sequenza, diagramma logico, porte logiche, tabella di verità, modellazione database, UML, Kotlin, Compose Multiplatform, open source, gratuito, Windows, macOS, Linux
url: https://jder.it/
---

# JDER — Editor di Diagrammi ER, Schemi Logici, Use Case, Class, Sequenza e Logici

**JDER** è un editor gratuito e open source per diagrammi **Entity-Relationship**, **schemi logici relazionali**, **Use Case**, **Class Diagram**, **diagrammi di sequenza** e **diagrammi logici**, scritto in Kotlin e Compose Multiplatform. Gira su Windows, macOS e Linux con un singolo file JAR.

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

## Caratteristiche Principali

### Interfaccia Moderna

Design Material 3 pulito e intuitivo con supporto tema chiaro/scuro e 20 palette di colori selezionabili.

### Esportazione Multipla

Salva i tuoi diagrammi in formato JSON o esportali come immagine PNG.

### Scorciatoie da Tastiera

Undo/Redo (`Ctrl+Z`/`Ctrl+Y`), Salva (`Ctrl+S`), Apri (`Ctrl+O`), Esporta (`Ctrl+E`), Zoom (`Ctrl+±`) e altro (`Cmd` su macOS).

### Note e Annotazioni

Aggiungi note e commenti testuali a qualsiasi punto del canvas.

### Veloce e Reattivo

Scritto in Kotlin con Compose Multiplatform per massime prestazioni su Windows, macOS e Linux.

## Screenshot

![Diagramma E/R in JDER: studenti, corsi e docenti di un'università](/er.png)

![Schema Logico in JDER: tabelle, chiavi primarie e chiavi esterne di un'università](/relational.png)

![Diagramma delle Classi in JDER: e-commerce con ereditarietà, interfacce ed enumerazioni](/class.png)

![Diagramma Use Case in JDER: sistema di gestione di una biblioteca](/usecase.png)

![Diagramma di Sequenza in JDER: prenotazione di un libro in una biblioteca](/sequence.png)

![Diagramma Logico in JDER: sommatore completo con porte XOR, AND e OR e valori simulati](/logic.png)

## Tecnologie

- Kotlin
- Compose Multiplatform
- Material 3
- Java

---

© 2025-2026 LoryPelli — [GitHub Repository](https://github.com/LoryPelli/JDER)
