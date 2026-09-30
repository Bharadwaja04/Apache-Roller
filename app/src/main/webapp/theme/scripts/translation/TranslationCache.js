/**
 * TranslationCache.js - IndexedDB cache layer for frontend translation.
 * Follows the Cache-Aside pattern.
 *
 * Exposes a global `TranslationCache` object with `get` and `put` methods,
 * and a `sha256` utility for hashing text.
 */

const DB_NAME = 'roller-translation-cache';
const DB_VERSION = 1;
const STORE_NAME = 'translations';

// Utility for hashing text to use in cache keys
async function sha256(message) {
  const msgUint8 = new TextEncoder().encode(message);
  const hashBuffer = await crypto.subtle.digest('SHA-256', msgUint8);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
  return hashHex;
}

const TranslationCache = {
  dbPromise: null,

  /**
   * Initialize IndexedDB
   * @returns {Promise<IDBDatabase>}
   */
  async init() {
    if (this.dbPromise) return this.dbPromise;

    if (!window.indexedDB) {
      console.warn('[TranslationCache] IndexedDB not supported, caching disabled.');
      return Promise.reject('IndexedDB not supported');
    }

    this.dbPromise = new Promise((resolve, reject) => {
      const request = indexedDB.open(DB_NAME, DB_VERSION);

      request.onupgradeneeded = (event) => {
        const db = event.target.result;
        if (!db.objectStoreNames.contains(STORE_NAME)) {
          // Key path is a composite string: URL|lang|provider|hash
          db.createObjectStore(STORE_NAME, { keyPath: 'id' });
        }
      };

      request.onsuccess = (event) => resolve(event.target.result);
      request.onerror = (event) => reject(event.target.error);
    });

    return this.dbPromise;
  },

  /**
   * Retrieve cached translation
   * @param {Object} key - { url, lang, provider, hash }
   * @returns {Promise<string|null>}
   */
  async get(key) {
    try {
      const db = await this.init();
      const id = `${key.url}|${key.lang}|${key.provider}|${key.hash}`;

      return new Promise((resolve, reject) => {
        const transaction = db.transaction([STORE_NAME], 'readonly');
        const store = transaction.objectStore(STORE_NAME);
        const request = store.get(id);

        request.onsuccess = () => {
          if (request.result) {
            resolve(request.result.translatedText);
          } else {
            resolve(null);
          }
        };
        request.onerror = () => resolve(null); // On error, treat as miss
      });
    } catch (e) {
      // Graceful fallback on missing DB
      return null;
    }
  },

  /**
   * Store translation in cache
   * @param {Object} key - { url, lang, provider, hash }
   * @param {string} translatedText
   * @returns {Promise<void>}
   */
  async put(key, translatedText) {
    if (!translatedText) return;

    try {
      const db = await this.init();
      const id = `${key.url}|${key.lang}|${key.provider}|${key.hash}`;
      
      const record = {
        id,
        url: key.url,
        language: key.lang,
        provider: key.provider,
        hash: key.hash,
        translatedText,
        timestamp: Date.now()
      };

      return new Promise((resolve, reject) => {
        const transaction = db.transaction([STORE_NAME], 'readwrite');
        const store = transaction.objectStore(STORE_NAME);
        const request = store.put(record);

        request.onsuccess = () => resolve();
        request.onerror = () => resolve(); // Ignore write errors
      });
    } catch (e) {
      // Ignore caching errors
    }
  }
};
