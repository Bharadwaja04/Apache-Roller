/**
 * translation.js - Main orchestrator script for Server-Side Translation.
 *
 * It provides a UI toolbar. When a user selects a language, it saves the
 * preference in cookies and reloads the page so the backend Filter can
 * intercept and translate the content dynamically.
 */

const LANGUAGES = [
  { code: 'en-IN', label: 'English' },
  { code: 'hi-IN', label: 'हिन्दी (Hindi)' },
  { code: 'ta-IN', label: 'தமிழ் (Tamil)' },
  { code: 'te-IN', label: 'తెలుగు (Telugu)' },
  { code: 'bn-IN', label: 'বাংলা (Bengali)' },
  { code: 'kn-IN', label: 'ಕನ್ನಡ (Kannada)' },
];

const DEFAULT_LANG    = 'en-IN';

function loadPrefs() {
  function getCookie(name) {
      const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
      return match ? match[2] : null;
  }
  return {
    lang:     getCookie('roller_translate_lang')     || DEFAULT_LANG,
    provider: getCookie('roller_translate_provider') || 'mymemory',
  };
}

function savePrefs(lang, provider) {
  document.cookie = `roller_translate_lang=${lang}; path=/; max-age=31536000`;
  document.cookie = `roller_translate_provider=${provider}; path=/; max-age=31536000`;
}

function clearPrefs() {
  document.cookie = "roller_translate_lang=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT";
  document.cookie = "roller_translate_provider=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT";
}

function isEditorPage() {
  const path = window.location.pathname + window.location.search;
  const editorPatterns = [
    '/roller-ui/authoring',
    'weblogEntry',
    'editWeblog',
    'newEntry',
    'editEntry',
    'editPages',
    'editPage',
    'MediaFileEdit',
    'MediaFileAdd',
    'bookmarkEdit',
    'categoryEdit',
    'commentManagement',
    'inviteMember',
  ];
  return editorPatterns.some(p => path.includes(p));
}

function buildTranslationUI() {
  const { lang: savedLang, provider: savedProvider } = loadPrefs();

  const toolbar = document.createElement('div');
  toolbar.id = 'roller-translation-bar';
  toolbar.innerHTML = `
    <style>
      #roller-translation-bar {
        display: flex; align-items: center; gap: 10px; padding: 5px 14px;
        background: #2c3e50; color: #ecf0f1; font-size: 13px; flex-wrap: wrap;
        position: sticky; top: 0; z-index: 9998;
      }
      #roller-translation-bar label { margin: 0; font-weight: 600; }
      #roller-lang-select, #roller-provider-select {
        padding: 3px 7px; border-radius: 4px; border: 1px solid #7f8c8d;
        background: #34495e; color: #ecf0f1; font-size: 13px; cursor: pointer;
      }
      #roller-translate-btn {
        padding: 4px 16px; border-radius: 4px; border: none; background: #27ae60;
        color: #fff; font-weight: 700; cursor: pointer; font-size: 13px;
        transition: background 0.2s;
      }
      #roller-translate-btn:hover { background: #219a52; }
      #roller-translate-btn.restore { background: #c0392b; }
      #roller-translate-btn.restore:hover { background: #a93226; }
      #roller-translate-status { font-size: 12px; color: #bdc3c7; }
    </style>

    <label for="roller-lang-select">🌐 Language:</label>
    <select id="roller-lang-select">
      ${LANGUAGES.map(l =>
        `<option value="${l.code}"${l.code === savedLang ? ' selected' : ''}>${l.label}</option>`
      ).join('')}
    </select>

    <label for="roller-provider-select">Provider:</label>
    <select id="roller-provider-select">
      <option value="sarvam"  ${savedProvider === 'sarvam'   ? 'selected' : ''}>Sarvam AI</option>
      <option value="mymemory"${savedProvider === 'mymemory' ? 'selected' : ''}>MyMemory API</option>
    </select>

    <button id="roller-translate-btn" class="${savedLang !== DEFAULT_LANG ? 'restore' : ''}">
      ${savedLang !== DEFAULT_LANG ? 'Restore English' : 'Translate'}
    </button>
    <span id="roller-translate-status">
      ${savedLang !== DEFAULT_LANG
        ? `<span style="color:#2ecc71">✓ ${savedProvider} — ${LANGUAGES.find(l => l.code === savedLang)?.label || savedLang}</span>`
        : ''}
    </span>
  `;

  document.body.insertAdjacentElement('afterbegin', toolbar);
  attachEventListeners();
}

function attachEventListeners() {
  const langSelect     = document.getElementById('roller-lang-select');
  const providerSelect = document.getElementById('roller-provider-select');
  const btn            = document.getElementById('roller-translate-btn');
  const statusSpan     = document.getElementById('roller-translate-status');

  const executeTranslation = async () => {
    const lang = langSelect.value;
    const providerName = providerSelect.value;
    
    if (lang === DEFAULT_LANG) return;
    
    savePrefs(lang, providerName);
    
    btn.disabled = true;
    statusSpan.innerHTML = '<span style="color:#f39c12">⏳ Translating...</span>';
    
    // Choose strategy
    const strategy = providerName === 'sarvam' 
      ? new SarvamStrategy() 
      : new FetchStrategy();
      
    const context = new TranslationContext(strategy);
    
    try {
      await translatePage(context, DEFAULT_LANG, lang);
      
      const langLabel = LANGUAGES.find(l => l.code === lang)?.label || lang;
      const providerLabel = providerName === 'sarvam' ? 'Sarvam AI' : 'MyMemory API';
      
      btn.textContent = 'Restore English';
      btn.classList.add('restore');
      statusSpan.innerHTML = `<span style="color:#2ecc71">✓ ${providerLabel} — ${langLabel}</span>`;
    } catch (err) {
      console.error('[Translation] Error:', err);
      statusSpan.innerHTML = '<span style="color:#e74c3c">❌ Translation failed</span>';
    } finally {
      btn.disabled = false;
    }
  };

  btn.addEventListener('click', async () => {
    if (btn.classList.contains('restore')) {
      clearPrefs();
      restorePage(); // Client-side restore
      
      btn.textContent = 'Translate';
      btn.classList.remove('restore');
      langSelect.value = DEFAULT_LANG;
      statusSpan.innerHTML = '';
      return;
    }
    
    await executeTranslation();
  });

  providerSelect.addEventListener('change', async () => {
    if (langSelect.value !== DEFAULT_LANG && btn.classList.contains('restore')) {
      await executeTranslation();
    }
  });
}

(function init() {
  if (isEditorPage()) return;
  
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', buildTranslationUI);
  } else {
    buildTranslationUI();
  }
})();
