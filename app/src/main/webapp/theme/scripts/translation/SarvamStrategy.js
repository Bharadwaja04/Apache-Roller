/**
 * SarvamStrategy.js - Concrete strategy that calls the Node.js proxy backend
 * which in turn uses the sarvamai SDK.
 *
 * This is Translation Method #1: Sarvam AI via SDK (backend proxy).
 * Supported endpoint: POST http://localhost:3001/api/translate/sarvam
 */
class SarvamStrategy extends TranslationStrategy {
  /**
   * @param {string} proxyUrl - Base URL of the Node.js proxy (default: http://localhost:3001)
   */
  constructor(proxyUrl = 'http://localhost:3001') {
    super();
    this.proxyUrl = proxyUrl.replace(/\/$/, '');
  }

  getName() {
    return 'Sarvam AI';
  }

  async translate(text, sourceLang, targetLang) {
    if (!text || !text.trim()) return text;
    if (sourceLang === targetLang) return text;

    try {
      const response = await fetch(`${this.proxyUrl}/api/translate/sarvam`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text, sourceLang, targetLang }),
      });

      if (!response.ok) {
        const errBody = await response.json().catch(() => ({}));
        throw new Error(errBody.error || `HTTP ${response.status}`);
      }

      const data = await response.json();
      return data.translatedText || text;
    } catch (err) {
      console.error('[SarvamStrategy] Error:', err.message);
      return text; // Graceful degradation — return original text on failure
    }
  }
}
