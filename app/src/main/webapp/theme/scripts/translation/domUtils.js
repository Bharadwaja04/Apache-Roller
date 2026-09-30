/**
 * domUtils.js - Utilities for walking the DOM and replacing text nodes.
 *
 * Rules:
 *  - Traverses text nodes only (never touches attributes, links, images, etc.)
 *  - Skips script, style, and non-content tags
 *  - Skips ALL form/editor elements (inputs, textareas, buttons, labels, selects)
 *  - Preserves the original text on each node so the page can be reverted
 */

const SKIP_TAGS = new Set([
  'SCRIPT', 'STYLE', 'NOSCRIPT', 'IFRAME', 'PRE', 'CODE', 'TEXTAREA',
  'INPUT', 'SELECT', 'OPTION', 'BUTTON', 'LABEL', 'SVG', 'MATH',
  // Rich text editor containers
  'FORM', 'FIELDSET',
]);

// Also skip any element with these classes/IDs (editor toolbars etc.)
const SKIP_CLASS_KEYWORDS = [
  'note-editor', 'note-toolbar', 'note-statusbar', 'note-editable',
  'CodeMirror', 'editor-', 'toolbar', 'datepicker', 'dropdown-menu',
];

/** Store originals keyed by the node object itself */
const originalTextMap = new WeakMap();

/**
 * Returns true if the node's ancestor chain contains an element we should skip.
 */
function isInsideSkippedContainer(node) {
  let el = node.parentElement;
  while (el) {
    if (SKIP_TAGS.has(el.tagName)) return true;
    const cls = el.className || '';
    if (typeof cls === 'string' && SKIP_CLASS_KEYWORDS.some(k => cls.includes(k))) return true;
    el = el.parentElement;
  }
  return false;
}

/**
 * Collect all visible text nodes under `root` that have non-whitespace content.
 * @param {Node} root
 * @returns {Text[]}
 */
function collectTextNodes(root) {
  const nodes = [];
  const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, {
    acceptNode(node) {
      if (!node.textContent.trim()) return NodeFilter.FILTER_SKIP;
      if (isInsideSkippedContainer(node)) return NodeFilter.FILTER_REJECT;
      return NodeFilter.FILTER_ACCEPT;
    },
  });

  let node;
  while ((node = walker.nextNode())) {
    nodes.push(node);
  }
  return nodes;
}

/**
 * Translate all text nodes on the page using the provided context.
 * @param {TranslationContext} context
 * @param {string} sourceLang
 * @param {string} targetLang
 * @param {function|null} onProgress - optional callback(done, total)
 */
async function translatePage(context, sourceLang, targetLang, onProgress) {
  const nodes = collectTextNodes(document.body);
  let done = 0;

  for (const node of nodes) {
    // Save original text before first translation
    if (!originalTextMap.has(node)) {
      originalTextMap.set(node, node.textContent);
    }

    const original = originalTextMap.get(node);
    const translated = await context.translate(original, sourceLang, targetLang);
    node.textContent = translated;

    done++;
    if (onProgress) onProgress(done, nodes.length);
  }
}

/**
 * Restore all text nodes to their English originals.
 */
function restorePage() {
  const nodes = collectTextNodes(document.body);
  for (const node of nodes) {
    if (originalTextMap.has(node)) {
      node.textContent = originalTextMap.get(node);
    }
  }
}
