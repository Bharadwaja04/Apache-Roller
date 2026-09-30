/**
 * Node.js Express backend proxy for Sarvam AI translation.
 * The frontend's SarvamStrategy calls POST /api/translate/sarvam on this server.
 * This server uses the official sarvamai SDK to talk to the Sarvam API securely.
 *
 * Run with:  node server.js
 * Default port: 3001
 */

import express from 'express';
import cors from 'cors';
import { SarvamAIClient } from 'sarvamai';

const app = express();
const PORT = process.env.PORT || 3001;
const SARVAM_API_KEY = process.env.SARVAM_API_KEY || 'sk_38y8kdez_wSCxoWXSV4d3kU1onSkXmcrW';

app.use(express.json({ limit: '5mb' }));

// Allow requests from the Roller webapp (adjust origin in production)
app.use(cors({
  origin: ['http://localhost:8080', 'http://localhost:8443', 'http://localhost:3000'],
  methods: ['POST'],
}));

// Lazily create the sarvam client once
const sarvamClient = new SarvamAIClient({ apiSubscriptionKey: SARVAM_API_KEY });

/**
 * POST /api/translate/sarvam
 * Body: { text: string, sourceLang: string, targetLang: string }
 * Response: { translatedText: string }
 */
app.post('/api/translate/sarvam', async (req, res) => {
  const { text, sourceLang = 'en-IN', targetLang } = req.body;

  if (!text || !targetLang) {
    return res.status(400).json({ error: 'text and targetLang are required' });
  }

  // If source and target are the same, just echo back
  if (sourceLang === targetLang) {
    return res.json({ translatedText: text });
  }

  try {
    const response = await sarvamClient.text.translate({
      input: text,
      source_language_code: sourceLang,
      target_language_code: targetLang,
      speaker_gender: 'Male',
      mode: 'formal',
      enable_preprocessing: true,
    });

    return res.json({ translatedText: response.translated_text || text });
  } catch (err) {
    console.error('[Sarvam] Error:', err?.message || err);
    return res.status(502).json({ error: 'Sarvam AI translation failed', details: err?.message });
  }
});

app.listen(PORT, () => {
  console.log(`[Sarvam Proxy] Listening on http://localhost:${PORT}`);
});
