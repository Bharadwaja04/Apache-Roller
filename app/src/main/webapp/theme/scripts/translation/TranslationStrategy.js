/**
 * TranslationStrategy.js - Abstract base class for the Strategy Pattern.
 * Place in: webapp/theme/scripts/translation/
 *
 * All concrete translation providers must extend this class and implement:
 *   - translate(text, sourceLang, targetLang) → Promise<string>
 *   - getName() → string
 */
class TranslationStrategy {
  /**
   * @param {string} text
   * @param {string} sourceLang  BCP-47 code, e.g. 'en-IN'
   * @param {string} targetLang  BCP-47 code, e.g. 'hi-IN'
   * @returns {Promise<string>}
   */
  async translate(text, sourceLang, targetLang) {
    throw new Error('translate() must be implemented by subclass');
  }

  /** @returns {string} Human-readable strategy name */
  getName() {
    throw new Error('getName() must be implemented by subclass');
  }
}
