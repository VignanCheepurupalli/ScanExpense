# ScanExpense — Receipt Scanner & Expense Tracker

An Android application that uses Google ML Kit to automatically scan receipts, extract expense data using OCR and custom parsing logic, and track spending with category breakdowns and visual analytics.

---

## Tech Stack

- **Language:** Java
- **Platform:** Android (minSdk 30, targetSdk 34)
- **ML / OCR:** Google ML Kit Text Recognition
- **Camera:** CameraX (Preview, ImageCapture, Lifecycle)
- **Database:** SQLite via `SQLiteOpenHelper`
- **Charts:** MPAndroidChart (PieChart)
- **Build:** Gradle (Kotlin DSL)

---

## Features

### Receipt Scanning (OCR)
- Captures receipt photos using CameraX with live preview
- Supports two camera modes: **SCAN** (full receipt flow) and **OCR** (raw text extraction preview)
- Passes captured image URI to `OcrProcessor`, which uses Google ML Kit's `TextRecognizer` to extract text from the image

### Intelligent Receipt Parsing
- `ReceiptParser` applies regex-based logic to extract three fields from raw OCR text:
  - **Vendor** — identifies the merchant name by looking for uppercase lines without numbers
  - **Date** — matches common date formats (`YYYY-MM-DD`, `MM/DD/YYYY`, `Month DD, YYYY`)
  - **Total** — first looks for a "Total: $X.XX" pattern; falls back to the largest dollar amount on the receipt

### Review & Save
- Extracted data is pre-filled into an editable form (`ReviewReceiptActivity`)
- User can correct any field before saving
- Category is assigned via a dropdown spinner (predefined categories)
- Data is saved to a local SQLite database

### Expense History
- Displays all saved receipts in a scrollable `RecyclerView` list
- Each row shows vendor, date, and total
- Tapping a receipt opens it for editing or deletion (`EditReceiptActivity`)
- Supports manual receipt entry (no camera required)
- Export to **PDF** — generates a multi-page PDF of the full expense history and opens it via FileProvider
- Export to **CSV** — writes all receipts to a `.csv` file and shares it via Android's share sheet

### Analytics Dashboard
- `DashboardActivity` queries the SQLite database and aggregates totals by category
- Renders a **Pie Chart** (MPAndroidChart) showing spending distribution across categories

### Authentication
- Simple login screen (`LoginActivity`) validates that email and password fields are non-empty before granting access

---

## App Architecture

```
LoginActivity
    └── MainActivity (Dashboard Hub)
            ├── CameraActivity (SCAN mode) → ReviewReceiptActivity → SQLite
            ├── CameraActivity (OCR mode)  → Raw text preview
            ├── HistoryActivity            → EditReceiptActivity
            └── DashboardActivity          → PieChart analytics
```

**Key classes:**

| Class | Responsibility |
|---|---|
| `OcrProcessor` | Wraps ML Kit TextRecognizer; exposes async callback interface |
| `ReceiptParser` | Regex-based extraction of vendor, date, and total from OCR text |
| `ReceiptDatabaseHelper` | SQLiteOpenHelper — CRUD operations for the receipt table |
| `ReceiptAdapter` | RecyclerView adapter for the expense history list |
| `Receipt` | Plain data model (id, vendor, date, total, category) |

---

## Getting Started

### Prerequisites
- Android Studio (Hedgehog or later recommended)
- Android device or emulator running API 30+

### Setup
1. Clone the repository
2. Open the project in Android Studio
3. Let Gradle sync and download dependencies
4. Run on a physical device (camera features require real hardware)

> **Note:** Camera and storage permissions are requested at runtime on first launch.

---

## Permissions Used

- `CAMERA` — for capturing receipt photos
- `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` — for PDF and CSV export
