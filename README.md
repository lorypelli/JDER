# JDER

**Java Diagrammi E/R** — A modern, cross-platform editor for Entity-Relationship, Use Case, and Class diagrams

[![Website](https://img.shields.io/badge/Website-jder.it-blue?style=for-the-badge)](https://jder.it/)
[![GitHub Release](https://img.shields.io/github/v/release/LoryPelli/JDER?style=for-the-badge)](https://github.com/LoryPelli/JDER/releases/latest)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE.md)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-purple?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-orange?style=for-the-badge&logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform)

[🌐 Website](https://jder.it/) • [⬇️ Download](https://github.com/LoryPelli/JDER/releases/download/JDER/JDER.jar) • [🐛 Issues](https://github.com/LoryPelli/JDER/issues)

---

## Overview

JDER is a complete rewrite in **Kotlin** and **Jetpack Compose** of the original software created by [Alessandro Ballini](https://ballini.it/Software/ProgER) and later maintained by [Gianvito Pio](https://gianvitopio.wordpress.com/jder). It brings a modern, clean Material 3 UI, new diagram types, and a significantly improved user experience.

---

## Features

### 🗂️ Diagram Types

#### Entity-Relationship (E/R) Diagram

- Create and manage **Entities** with full attribute support
  - Normal, Key (primary key), Multivalued, Derived, and Composite attributes
  - Weak entity support
- Create **Relationships** between entities with full cardinality notation
  - Supported cardinalities: `1`, `(0,1)`, `(1,1)`, `N`, `(0,N)`, `(1,N)`
  - Associative entity conversion (N-to-N relationships)
- Add **textual Notes** anywhere on the canvas
- Properties Panel for editing elements and connections
- Context menu for quick actions on any element

#### Class Diagram

- Create **Classes**, **Interfaces**, **Enums**, and **Abstract Classes** (with UML stereotype labels)
- Add **Attributes** and **Methods** to each class with:
  - Visibility modifiers: Public (`+`), Private (`-`), Protected (`#`), Package (`~`)
  - Static and abstract member flags
  - Type annotation for each member
- Draw **Relations** between classes:
  - Association, Aggregation, Composition, Inheritance, Realization, Dependency
  - Source and target multiplicity labels on each relation
  - Optional relation label
- Add inline **documentation** to any class
- Add **textual Notes** shared with the other diagram note component

#### Use Case Diagram

- Create **Actors** (with stick figure representation)
- Create **Use Cases** (with ellipse representation)
- Draw **Relations** between elements:
  - Association, Include (`<<include>>`), Extend (`<<extend>>`), Generalization
  - Fully rendered arrow heads with correct directionality
- Define **System Boundaries** to group use cases
- Add **textual Notes** shared with the E/R note component

---

### 🎨 Interface & UX

- **Material 3** design system throughout the entire app
- **Light & Dark mode** with smooth animated transition
- **20 official Material color palettes** selectable at runtime:
  - Red, Pink, Purple, Deep Purple, Indigo, Blue, Light Blue, Cyan, Teal, Green, Light Green, Lime, Yellow, Amber, Orange, Deep Orange, Brown, Gray, Blue Gray, Black & White
- **Tab-based navigation** between E/R and Use Case diagrams
- **Properties Panel** — context-aware sidebar for editing selected elements
- **Context Menu** — right-click on any canvas element for quick actions

---

### 🖱️ Canvas Controls

| Action            | Control                           |
| ----------------- | --------------------------------- |
| Zoom In           | `Ctrl` + `+` or mouse scroll up   |
| Zoom Out          | `Ctrl` + `-` or mouse scroll down |
| Pan               | Middle mouse button drag          |
| Select & Move     | Left click + drag                 |
| Open context menu | Right click on element            |
| Reset Zoom        | Vista menu → Reimposta Zoom       |

---

### ⌨️ Keyboard Shortcuts

| Shortcut           | Action                  |
| ------------------ | ----------------------- |
| `Ctrl + Z`         | Undo                    |
| `Ctrl + Y`         | Redo                    |
| `Ctrl + S`         | Save                    |
| `Ctrl + Shift + S` | Save As                 |
| `Ctrl + O`         | Open                    |
| `Ctrl + N`         | New Diagram             |
| `Ctrl + +`         | Zoom In                 |
| `Ctrl + -`         | Zoom Out                |
| `Delete`           | Delete selected element |

---

### 💾 Save & Export

- Save diagrams in **JSON** format (`.json`)
- Open and reload previously saved diagrams
- Export any diagram as a **PNG image**
- Unsaved changes dialog on close or when opening a new diagram
- Global undo/redo history (up to 50 states)

---

## Stack

| Technology                                                                   | Purpose              |
| ---------------------------------------------------------------------------- | -------------------- |
| [Kotlin](https://kotlinlang.org/)                                            | Primary language     |
| [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) | UI framework         |
| [Material 3](https://m3.material.io/)                                        | Design system        |
| [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)     | JSON save/load       |
| [Gradle](https://gradle.org/)                                                | Build system         |
| [Cloudflare Workers](https://workers.cloudflare.com/)                        | Website hosting      |
| [Skia](https://skia.org/)                                                    | PNG rendering/export |
| Java 8                                                                       | JVM target           |

---

## Requirements

- **Java 8** or higher
- Windows, macOS (Intel or Apple Silicon), or Linux (x64 or ARM)

---

## Getting Started

### Download

Download the latest `JDER.jar` from the [Releases page](https://github.com/LoryPelli/JDER/releases/latest) or directly:

```
https://github.com/LoryPelli/JDER/releases/download/JDER/JDER.jar
```

Then run it with:

```bash
java -jar JDER.jar
```

### Build from source

```bash
git clone https://github.com/LoryPelli/JDER.git
cd JDER
./gradlew run
```

To build a standalone JAR:

```bash
./gradlew jar
```

The output will be in `build/libs/JDER.jar`.

---

## Project Structure

```
src/
└── main/
    ├── kotlin/com/jder/
    │   ├── Main.kt                    # App entry point
    │   ├── data/
    │   │   ├── DiagramRepository.kt        # E/R JSON persistence
    │   │   ├── UseCaseRepository.kt        # Use Case JSON persistence
    │   │   ├── ClassDiagramRepository.kt   # Class Diagram JSON persistence
    │   │   ├── ImageExporter.kt            # PNG export via Skia
    │   │   └── JsonConfig.kt               # kotlinx.serialization config
    │   ├── domain/model/
    │   │   ├── ERModels.kt                 # Entity, Relationship, Attribute, Note data classes
    │   │   ├── UseCaseModels.kt            # Actor, UseCase, SystemBoundary data classes
    │   │   ├── ClassDiagramModels.kt       # ClassEntity, ClassMember, ClassRelation data classes
    │   │   ├── DiagramState.kt             # E/R state + undo/redo
    │   │   ├── UseCaseState.kt             # Use Case state + undo/redo
    │   │   ├── ClassDiagramState.kt        # Class Diagram state + undo/redo
    │   │   ├── ToolMode.kt                 # E/R tool modes
    │   │   ├── UseCaseToolMode.kt          # Use Case tool modes
    │   │   └── ClassDiagramToolMode.kt     # Class Diagram tool modes
    │   └── ui/
    │       ├── components/            # Reusable UI components
    │       ├── dialogs/               # Property & creation dialogs
    │       ├── screens/               # AppScreen, MainScreen, UseCaseScreen, ClassDiagramScreen
    │       ├── theme/                 # Theme, ColorPalettes, ThemeState
    │       └── utils/                 # Diagram & PNG renderers
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
