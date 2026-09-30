/**
 * TranslationContext.js - Context class for the Strategy Pattern.
 * Manages the current translation strategy and exposes a unified translate() API.
 * Strategies can be swapped at runtime without changing the calling code.
 */
class TranslationContext {
  /**
   * @param {TranslationStrategy} strategy - Initial strategy
   */
  constructor(strategy) {
    this._strategy = strategy;
  }

  /**
   * Swap the active translation strategy.
   * @param {TranslationStrategy} strategy
   */
  setStrategy(strategy) {
    this._strategy = strategy;
  }

  /** @returns {TranslationStrategy} */
  getStrategy() {
    return this._strategy;
  }

  /**
   * Translate text using the currently active strategy, with IndexedDB caching.
   * @param {string} text
   * @param {string} sourceLang
   * @param {string} targetLang
   * @returns {Promise<string>}
   */
  async translate(text, sourceLang, targetLang) {
    if (!text || !text.trim()) return text;
    if (sourceLang === targetLang) return text;
  
    // 1. Compute hash of the original text
    const hash = await sha256(text);
    
    // 2. Build cache key
    const cacheKey = {
      url: location.pathname,
      lang: targetLang,
      provider: this._strategy.getName(),
      hash
    };

    // 3. Check IndexedDB cache
    const cached = await TranslationCache.get(cacheKey);
    if (cached) {
      return cached;
    }

    // 4. Cache miss -> Call provider strategy
    const result = await this._strategy.translate(text, sourceLang, targetLang);

    // 5. Store in cache if successful
    if (result && result !== text) {
      await TranslationCache.put(cacheKey, result);
    }

    return result;
  }
}
