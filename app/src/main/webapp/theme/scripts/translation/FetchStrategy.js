/**
 * FetchStrategy.js - Concrete strategy using the free MyMemory public API.
 * This is Translation Method #2: HTTP-based translation via MyMemory REST API.
 *
 * It is a completely different approach from SarvamStrategy:
 * - No backend proxy needed
 * - Uses the browser's native fetch() to call a public REST API directly
 * - Supports the same 5 language codes
 *
 * MyMemory API: https://api.mymemory.translated.net/get?q=...&langpair=en|hi
 */
class FetchStrategy extends TranslationStrategy {
  getName() {
    return 'MyMemory API';
  }

  /**
   * MyMemory uses simple 2-letter ISO codes or "en-IN" style codes.
   * We strip the trailing region for its API (e.g. 'hi-IN' → 'hi').
   */
  _toMyMemoryCode(bcp47) {
    return bcp47.split('-')[0];
  }

  async translate(text, sourceLang, targetLang) {
    if (!text || !text.trim()) return text;
    if (sourceLang === targetLang) return text;

    const src = this._toMyMemoryCode(sourceLang);
    const tgt = this._toMyMemoryCode(targetLang);

    try {
      const url = `https://api.mymemory.translated.net/get?q=${encodeURIComponent(text)}&langpair=${src}|${tgt}`;
      const response = await fetch(url);
      if (!response.ok) throw new Error(`HTTP ${response.status}`);

      const data = await response.json();
      const translated = data?.responseData?.translatedText;
      // MyMemory returns QUERY LIMIT REACHED on free tier exhaustion
      if (!translated || translated.includes('QUERY LIMIT')) {
        throw new Error('MyMemory query limit reached or no result');
      }
      return translated;
    } catch (err) {
      console.error('[FetchStrategy] Error:', err.message);
      return text; // Graceful degradation
    }
  }
}
