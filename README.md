# NoteSR-Cli

**NoteSR-Cli** is a cross-platform command-line utility designed to process exports from [NoteSR](https://github.com/zHd4/NoteSR).

[![GitHub release](https://img.shields.io/github/v/release/zHd4/NoteSR-Cli)](https://github.com/zHd4/NoteSR-Cli/releases)
[![Java CI](https://github.com/zHd4/NoteSR-Cli/actions/workflows/ci.yml/badge.svg)](https://github.com/zHd4/NoteSR-Cli/actions/workflows/ci.yml)
[![Maintainability](https://qlty.sh/gh/zHd4/projects/NoteSR-Cli/maintainability.svg)](https://qlty.sh/gh/zHd4/projects/NoteSR-Cli)
[![Code Coverage](https://qlty.sh/gh/zHd4/projects/NoteSR-Cli/coverage.svg)](https://qlty.sh/gh/zHd4/projects/NoteSR-Cli)

## 🚀 Quick Start

```bash
git clone https://github.com/zHd4/NoteSR-Cli.git
cd NoteSR-Cli
./gradlew build

# After build
cd build/notesr-cli/bin
./notesr-cli --help
```

---

## 🎯 What It Does

NoteSR-CLI allows you to:

* 🔓 Decrypt `.notesr.bak` backups from NoteSR (AES-256 encrypted)
* 🧠 Read notes and list files directly from the SQLite database
* 📦 Compile a SQLite backup into a valid `.notesr.bak` file
* 📌 Attach and extract files from notes
* 🔍 Inspect and manage your encrypted notes via terminal

---

## ⚙️ Commands Overview

### 🟢 General Help

```bash
./notesr-cli --help
```

---

### 🔓 `decrypt`

Decrypts a NoteSR `.notesr.bak` file into a readable SQLite database.

```bash
./notesr-cli decrypt /path/to/backup.notesr.bak /path/to/crypto_key.txt -o output.notesr.db
```

* `file_path` — path to the encrypted `.notesr.bak` file
* `key_path` — path to the AES key file (text format)
* `-o` — optional output path (defaults to `bak_file_name.notesr.db`)

---

### 📦 `compile`

Compiles a SQLite NoteSR backup into an encrypted `.notesr.bak` file.

```bash
./notesr-cli compile notes.notesr.db crypto_key.txt -o backup.notesr.bak -n 1.1.0
```

* `db_path` — SQLite database file
* `key_path` — AES key file (text)
* `-o` — output `.notesr.bak` file
* `-n` — optional NoteSR version (default is shown via `--version`)

---

### 📄 `list-notes`

Lists all notes from the decrypted SQLite database.

```bash
./notesr-cli list-notes notes.notesr.db
```

---

### 🧒 `read-note`

Displays the full content of a specific note.

```bash
./notesr-cli read-note notes.notesr.db NOTE_ID
```

---

### 📁 `list-files`

Lists all files attached to a specific note.

```bash
./notesr-cli list-files notes.notesr.db NOTE_ID
```

---

### 📥 `get-file`

Extracts a file attached to a note and saves it to disk.

```bash
./notesr-cli get-file notes.notesr.db NOTE_ID FILE_ID -o ./output_dir
```

* `NOTE_ID` — ID of the note
* `FILE_ID` — ID of the file attachment
* `-o` — output file or directory path (optional)

---

### 📤 `put-file`

Attaches a local file to a specific note.

```bash
./notesr-cli put-file notes.notesr.db NOTE_ID /path/to/local/file.jpg
```

---

## 🧪 Run Tests

```bash
./gradlew test
```

---

## ⚖️ Requirements

* Java 21+
* Gradle (optional — project includes wrapper)

---

## 📌 Project Details

* Language: **Java**
* CLI Framework: **Picocli**
* DB Layer: **JDBI** (SQLite)
* Crypto: **AES-256**, compatible with NoteSR Android
* Logging: **SLF4J + SimpleLogger**
