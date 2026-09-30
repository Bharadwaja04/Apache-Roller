# Web Translation Feature - README

## Overview
This feature adds **in-page web translation** to Apache Roller using the **Strategy Design Pattern** in plain JavaScript. No React, no Vite, no build step required for the frontend.

## Architecture

```
frontend/               ← Node.js backend proxy (for Sarvam AI SDK)
  server.js             ← Express server exposing POST /api/translate/sarvam
  package.json          ← express, cors, sarvamai

webapp/theme/scripts/translation/   ← Plain JS frontend files (loaded via <script> tags)
  TranslationStrategy.js  ← Abstract base class (Strategy interface)
  SarvamStrategy.js       ← Concrete Strategy #1: calls backend proxy
  FetchStrategy.js        ← Concrete Strategy #2: calls MyMemory REST API directly
  TranslationContext.js   ← Context class — manages active strategy
  domUtils.js             ← DOM walker that replaces text nodes safely
  translation.js          ← Main orchestrator: builds UI, handles events
```

## Strategy Pattern

| Role               | Class                   |
|--------------------|-------------------------|
| Strategy Interface | `TranslationStrategy`   |
| Concrete Strategy 1| `SarvamStrategy`        |
| Concrete Strategy 2| `FetchStrategy`         |
| Context            | `TranslationContext`    |

**Two distinct translation methods:**
1. **Sarvam AI (SDK)** – calls the Node.js backend which uses the official `sarvamai` npm SDK
2. **MyMemory API** – calls the free public MyMemory REST API directly from the browser via `fetch()`

## Supported Languages
| Language | BCP-47 Code |
|----------|-------------|
| English  | `en-IN`     |
| Hindi    | `hi-IN`     |
| Tamil    | `ta-IN`     |
| Telugu   | `te-IN`     |
| Bengali  | `bn-IN`     |
| Kannada  | `kn-IN`     |

All languages work as both source and target.

## Running

### 1. Start the Sarvam AI backend proxy
```bash
cd frontend
npm install      # only needed once
node server.js   # starts on http://localhost:3001
```
Set the API key via environment variable (optional — key is already set as default in server.js):
```bash
SARVAM_API_KEY=sk_xxxxxx node server.js
```

### 2. Start Apache Roller
```bash
cd app
mvn -pl . jetty:run
```

### 3. Translate
- Open any page in Roller
- Use the **language toolbar** at the top of the page
- Select a language and click **Translate**
- Switch provider with the **Provider** dropdown (Sarvam AI ↔ MyMemory API)
- Click **Restore** to go back to English

## How the DOM Translation Works
- Uses `TreeWalker` to find text nodes only — links, images, attributes are never touched
- Skips `<script>`, `<style>`, `<noscript>`, `<pre>`, `<code>`, and other non-content tags
- Saves original English text in a `WeakMap` before first translation
- Restore is instant (no API call needed)
- Layout is fully preserved as only `textContent` changes
