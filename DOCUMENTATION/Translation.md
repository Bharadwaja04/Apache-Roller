# Translation Feature Documentation

**Feature:** Web Page Translation  
**Assignment:** Project 1 — Software Engineering (IIIT-H)  
**Marks:** 20  
**Date:** March 2026

---

## Table of Contents

1. [Overview](#overview)
2. [Design Pattern Used](#design-pattern-used)
3. [Translation Methods](#translation-methods)
4. [Supported Languages](#supported-languages)
5. [Architecture Diagram](#architecture-diagram)
6. [Folder and File Structure](#folder-and-file-structure)
7. [File-by-File Details](#file-by-file-details)
8. [How It Works — Step by Step](#how-it-works--step-by-step)
9. [Modified Existing Files](#modified-existing-files)
10. [Running the Feature](#running-the-feature)
11. [Design Decisions](#design-decisions)

---

## Overview

This feature adds **application-sided web translation** to Apache Roller. When a user selects a language from the navbar toolbar, all visible text on the page is translated in-place — links, images, and layout remain completely unchanged. Only `textContent` of text nodes changes.

The selected language is **persisted in `localStorage`** so that every subsequent page the user navigates to is automatically translated without requiring any input from the user.

---

## Design Pattern Used

### Strategy Pattern

The translation feature is built around the **Strategy Design Pattern**.

| Role | Class | Description |
|------|-------|-------------|
| **Strategy Interface** | `TranslationStrategy` | Abstract base class defining the contract (`translate()`, `getName()`) |
| **Concrete Strategy 1** | `SarvamStrategy` | Calls a Node.js proxy backend which uses the Sarvam AI SDK |
| **Concrete Strategy 2** | `FetchStrategy` | Calls the MyMemory public REST API directly from the browser |
| **Context** | `TranslationContext` | Holds the current strategy and delegates `translate()` calls to it |

The **Context** (`TranslationContext`) is the only class the UI code interacts with. The active strategy can be swapped at runtime (via the Provider dropdown) without changing any calling code. This is the core benefit of the Strategy Pattern.

```
UI code
  │
  ▼
TranslationContext.translate(text, src, tgt)
  │
  ├─── [if strategy = SarvamStrategy]  ──▶  POST /api/translate/sarvam (Node.js proxy)
  │                                              │
  │                                              └──▶  sarvamai SDK  ──▶  Sarvam AI API
  │
  └─── [if strategy = FetchStrategy]   ──▶  GET api.mymemory.translated.net (browser fetch)
```

---

## Translation Methods

### Method 1 — Sarvam AI (via SDK)

- **Provider:** [Sarvam AI](https://sarvam.ai)
- **SDK:** `sarvamai` npm package (official)
- **How it works:** The browser calls a local Node.js Express server (`frontend/server.js`). The server uses the `SarvamAIClient` from the `sarvamai` SDK to call the Sarvam translation API and returns the result.
- **Why a backend proxy?** The `sarvamai` SDK is a Node.js package and cannot run in the browser. The proxy keeps the API key secure on the server.
- **API Key:** `sk_38y8kdez_wSCxoWXSV4d3kU1onSkXmcrW`
- **Endpoint used:** `client.text.translate()`

### Method 2 — MyMemory API (via browser fetch)

- **Provider:** [MyMemory](https://api.mymemory.translated.net)
- **How it works:** The browser calls the MyMemory public REST API directly using the native `fetch()` API. No backend proxy is needed.
- **API URL:** `https://api.mymemory.translated.net/get?q=<text>&langpair=<src>|<tgt>`
- **Key difference from Method 1:** This is a completely different translation approach — direct HTTP call from the browser to a public API, no SDK, no backend involved.

> These two methods satisfy the requirement of "two different ways of translating", with one being Sarvam AI.

---

## Supported Languages

| Language | BCP-47 Code | Script |
|----------|-------------|--------|
| English  | `en-IN` | Latin |
| Hindi    | `hi-IN` | Devanagari |
| Tamil    | `ta-IN` | Tamil |
| Telugu   | `te-IN` | Telugu |
| Bengali  | `bn-IN` | Bengali |
| Kannada  | `kn-IN` | Kannada |

All 6 languages work as both **source and target**. The default source is English (`en-IN`).

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                   Apache Roller (Java / JSP)                │
│                                                             │
│  head.jsp  ──loads──▶  translation/TranslationStrategy.js  │
│                        translation/SarvamStrategy.js        │
│                        translation/FetchStrategy.js         │
│                        translation/TranslationContext.js    │
│                        translation/domUtils.js              │
│                        translation/translation.js  ◀── UI  │
└─────────────────────────────────┬───────────────────────────┘
                                  │ SarvamStrategy: POST
                                  ▼
                    ┌─────────────────────────┐
                    │  frontend/server.js      │
                    │  (Node.js + Express)     │
                    │  Port: 3001              │
                    └────────────┬────────────┘
                                 │ sarvamai SDK
                                 ▼
                    ┌─────────────────────────┐
                    │     Sarvam AI API        │
                    │  (cloud translation)     │
                    └─────────────────────────┘

  FetchStrategy: browser fetch()
  ──────────────────────────────▶  api.mymemory.translated.net
```

---

## Folder and File Structure

### New Files Created

```
project-1-team-20/
│
├── frontend/                          ← NEW: Node.js backend proxy
│   ├── package.json                   ← npm config (express, cors, sarvamai)
│   ├── server.js                      ← Express proxy server for Sarvam AI
│   └── node_modules/                  ← Installed by: npm install
│
├── app/src/main/webapp/
│   └── theme/scripts/
│       └── translation/               ← NEW: All frontend JS files
│           ├── TranslationStrategy.js ← Abstract base class (Strategy interface)
│           ├── SarvamStrategy.js      ← Concrete Strategy #1 (Sarvam AI)
│           ├── FetchStrategy.js       ← Concrete Strategy #2 (MyMemory API)
│           ├── TranslationContext.js  ← Context class (Strategy Pattern)
│           ├── domUtils.js            ← DOM text-node walker and replacer
│           └── translation.js        ← Main orchestrator + UI toolbar
│
└── DOCUMENTATION/
    └── Translation.md                 ← This file
```

### Modified Existing Files

```
app/src/main/webapp/WEB-INF/jsps/tiles/
    └── head.jsp                       ← MODIFIED: Added 6 <script> tags to load translation JS files
```

---

## File-by-File Details

### `frontend/package.json`
npm manifest for the Node.js backend. Dependencies:
- `express` — HTTP server framework
- `cors` — allows cross-origin requests from Roller webapp
- `sarvamai` — official Sarvam AI Node.js SDK

### `frontend/server.js`
Express server that runs on **port 3001**. Exposes a single endpoint:

```
POST /api/translate/sarvam
Body:  { text: string, sourceLang: string, targetLang: string }
Response: { translatedText: string }
```

Uses `SarvamAIClient` from `sarvamai` package. The call is:
```js
await sarvamClient.text.translate({ input, source_language_code, target_language_code, ... })
```

### `translation/TranslationStrategy.js`
Abstract base class defining the Strategy interface. All subclasses must implement:
- `translate(text, sourceLang, targetLang)` → `Promise<string>`
- `getName()` → `string`

### `translation/SarvamStrategy.js`
Implements `TranslationStrategy`. Sends a `POST` request to `http://localhost:3001/api/translate/sarvam`. On error, gracefully returns the original text (no crash).

### `translation/FetchStrategy.js`
Implements `TranslationStrategy`. Calls `https://api.mymemory.translated.net/get` directly from the browser using `fetch()`. Converts BCP-47 codes to ISO 2-letter codes (e.g. `hi-IN` → `hi`) for the MyMemory API.

### `translation/TranslationContext.js`
The Strategy Pattern Context. Holds a reference to the current strategy and delegates:
```js
const context = new TranslationContext(sarvamStrategy);
context.setStrategy(fetchStrategy);       // switch at runtime
await context.translate(text, src, tgt); // calls whatever strategy is active
```

### `translation/domUtils.js`
DOM traversal utility. Uses `document.createTreeWalker()` to find text nodes only. 

**Nodes skipped (never translated):**
- `SCRIPT`, `STYLE`, `NOSCRIPT`, `IFRAME`, `PRE`, `CODE`
- `INPUT`, `TEXTAREA`, `SELECT`, `OPTION`, `BUTTON`, `LABEL`, `FORM`, `FIELDSET`
- Any element with Summernote editor CSS classes (`note-editor`, `note-toolbar`, etc.)

**Key functions:**
- `translatePage(context, src, tgt, onProgress)` — walks DOM and translates every text node
- `restorePage()` — restores all nodes to their original English text (stored in a `WeakMap`)

### `translation/translation.js`
Main orchestrator. Responsibilities:
1. **Language persistence** — reads/writes `roller_translate_lang` and `roller_translate_provider` in `localStorage`
2. **Auto-translate on page load** — if a language is saved, hides the page with `visibility:hidden`, then translates and reveals
3. **Loading overlay** — dark full-screen spinner shown during translation; page is only revealed when 100% done
4. **Editor page detection** (`isEditorPage()`) — skips auto-translate on authoring URLs (New Entry, Edit Weblog, etc.) to prevent slowness
5. **Toolbar UI** — injects a sticky bar at the top of every page with Language dropdown, Provider dropdown, Translate/Restore button

---

## How It Works — Step by Step

### First Use (User selects a language)

1. User opens any Roller page
2. The translation toolbar appears at the top (English selected by default)
3. User picks **Hindi** from the Language dropdown and clicks **Translate**
4. A dark full-screen **spinner overlay** appears immediately (page content hidden)
5. `translation.js` calls `doTranslate('hi-IN', 'sarvam', false)`
6. `restorePage()` is called first (in case of re-translate)
7. `translatePage()` from `domUtils.js` walks all text nodes and calls `context.translate()` for each
8. `SarvamStrategy.translate()` sends `POST /api/translate/sarvam` to `frontend/server.js`
9. `server.js` calls `sarvamClient.text.translate()` and returns translated text
10. Each text node is updated; layout/links/images are untouched
11. `savePrefs('hi-IN', 'sarvam')` saves to `localStorage`
12. Spinner disappears; fully-translated page is revealed

### Subsequent Pages (Auto-translate)

1. User navigates to another page
2. `translation.js` runs immediately; reads `localStorage` → Hindi saved
3. `body { visibility: hidden }` is injected into `<head>` before DOM renders
4. On `DOMContentLoaded`: overlay spinner appears, `visibility:hidden` is removed
5. Auto-translation runs silently; page revealed when done

### Restoring English

1. User clicks **Restore English**
2. `restorePage()` restores all original text nodes from the `WeakMap` cache (instant, no API call)
3. `localStorage` is cleared
4. Future pages load in English normally

---

## Modified Existing Files

### `app/src/main/webapp/WEB-INF/jsps/tiles/head.jsp`

Added 6 `<script>` tags after the existing `roller.js` include. Order is critical (dependencies must load before dependents):

```jsp
<%-- ===== Web Translation: Strategy Pattern scripts (order matters) ===== --%>
<script src="<s:url value="/theme/scripts/translation/TranslationStrategy.js"/>"></script>
<script src="<s:url value="/theme/scripts/translation/SarvamStrategy.js"/>"></script>
<script src="<s:url value="/theme/scripts/translation/FetchStrategy.js"/>"></script>
<script src="<s:url value="/theme/scripts/translation/TranslationContext.js"/>"></script>
<script src="<s:url value="/theme/scripts/translation/domUtils.js"/>"></script>
<script src="<s:url value="/theme/scripts/translation/translation.js"/>"></script>
```

Since these are plain non-module scripts, they share the global scope — no bundler needed.

---

## Running the Feature

### Step 1: Install backend dependencies (one time only)
```bash
cd frontend
npm install
```

### Step 2: Start the Sarvam AI proxy server
```bash
node server.js
# Output: [Sarvam Proxy] Listening on http://localhost:3001
```

To use a different API key:
```bash
SARVAM_API_KEY=sk_your_key_here node server.js
```

### Step 3: Start Apache Roller
```bash
# From project root
mvn -pl app jetty:run
# Then open: http://localhost:8080/roller
```

### Step 4: Translate
- Open any page in Roller
- Use the **🌐 Language** dropdown to pick a language
- Click **Translate**
- The page hides, translates fully, then reappears in the chosen language
- All subsequent pages auto-translate — no further interaction needed
- Click **Restore English** to revert and stop auto-translation

---

## Design Decisions

| Decision | Rationale |
|----------|-----------|
| Strategy Pattern | Decouples UI from translation logic; swapping providers requires zero change to calling code |
| Node.js proxy for Sarvam | The `sarvamai` SDK is Node.js only; proxy also keeps API key off the client |
| Plain JS (no React/Vite) | Integrates directly with existing JSP/Java project without any build pipeline changes |
| `localStorage` for persistence | Simple, zero-server-round-trip, works across all pages without session complexity |
| `WeakMap` for originals | Memory-safe storage of original text; no static Map that could grow unboundedly |
| `TreeWalker` for DOM | Most efficient DOM traversal API; avoids innerHTML manipulation which would break event listeners |
| Skip editor pages | Roller's Summernote editor has hundreds of toolbar nodes; translating them adds no value and causes 60+ seconds of delay |
| `visibility:hidden` before DOMContentLoaded | Prevents any flash of English content before the translation overlay can mount |
