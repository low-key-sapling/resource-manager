<template>
  <div class="markdown-preview" v-html="renderedContent"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import { markedHighlight } from 'marked-highlight'

// 配置marked使用highlight.js
marked.use(markedHighlight({
  langPrefix: 'hljs language-',
  highlight(code, lang) {
    const language = hljs.getLanguage(lang) ? lang : 'plaintext'
    return hljs.highlight(code, { language }).value
  }
}))

const props = defineProps<{
  content: string
}>()

const renderedContent = computed(() => {
  try {
    return marked(props.content)
  } catch {
    return '<p>渲染错误</p>'
  }
})
</script>

<style scoped>
.markdown-preview {
  padding: var(--spacing-lg);
  line-height: 1.7;
  color: var(--text-primary);
}

.markdown-preview :deep(h1),
.markdown-preview :deep(h2),
.markdown-preview :deep(h3),
.markdown-preview :deep(h4),
.markdown-preview :deep(h5),
.markdown-preview :deep(h6) {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.25;
  color: var(--text-primary);
}

.markdown-preview :deep(h1) { 
  font-size: 2em; 
  border-bottom: 1px solid var(--border-color); 
  padding-bottom: 0.3em;
}

.markdown-preview :deep(h2) { 
  font-size: 1.5em; 
  border-bottom: 1px solid var(--border-color); 
  padding-bottom: 0.3em;
}

.markdown-preview :deep(h3) { font-size: 1.25em; }
.markdown-preview :deep(h4) { font-size: 1em; }

.markdown-preview :deep(p) {
  margin-bottom: 16px;
}

.markdown-preview :deep(a) {
  color: var(--primary-color);
  text-decoration: none;
}

.markdown-preview :deep(a:hover) {
  text-decoration: underline;
}

.markdown-preview :deep(code) {
  background-color: var(--bg-tertiary);
  padding: 0.2em 0.4em;
  border-radius: var(--radius-sm);
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 0.9em;
}

.markdown-preview :deep(pre) {
  background-color: #1e1e1e;
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  overflow-x: auto;
  margin-bottom: 16px;
}

.markdown-preview :deep(pre code) {
  background: none;
  padding: 0;
  color: #d4d4d4;
  font-size: 14px;
  line-height: 1.5;
}

.markdown-preview :deep(ul),
.markdown-preview :deep(ol) {
  margin-bottom: 16px;
  padding-left: 2em;
}

.markdown-preview :deep(li) {
  margin-bottom: 4px;
}

.markdown-preview :deep(blockquote) {
  border-left: 4px solid var(--primary-color);
  padding: var(--spacing-sm) var(--spacing-md);
  margin: 16px 0;
  background: var(--bg-secondary);
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
  color: var(--text-secondary);
}

.markdown-preview :deep(blockquote p:last-child) {
  margin-bottom: 0;
}

.markdown-preview :deep(img) {
  max-width: 100%;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.markdown-preview :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 16px;
}

.markdown-preview :deep(th),
.markdown-preview :deep(td) {
  border: 1px solid var(--border-color);
  padding: var(--spacing-sm) var(--spacing-md);
  text-align: left;
}

.markdown-preview :deep(th) {
  background: var(--bg-secondary);
  font-weight: 600;
}

.markdown-preview :deep(tr:nth-child(even)) {
  background: var(--bg-secondary);
}

.markdown-preview :deep(hr) {
  border: none;
  border-top: 1px solid var(--border-color);
  margin: 24px 0;
}

/* highlight.js 主题样式 */
.markdown-preview :deep(.hljs-keyword) { color: #569cd6; }
.markdown-preview :deep(.hljs-string) { color: #ce9178; }
.markdown-preview :deep(.hljs-number) { color: #b5cea8; }
.markdown-preview :deep(.hljs-comment) { color: #6a9955; }
.markdown-preview :deep(.hljs-function) { color: #dcdcaa; }
.markdown-preview :deep(.hljs-class) { color: #4ec9b0; }
.markdown-preview :deep(.hljs-variable) { color: #9cdcfe; }
.markdown-preview :deep(.hljs-attr) { color: #9cdcfe; }
.markdown-preview :deep(.hljs-tag) { color: #569cd6; }
.markdown-preview :deep(.hljs-attribute) { color: #9cdcfe; }
.markdown-preview :deep(.hljs-built_in) { color: #4ec9b0; }
</style>
