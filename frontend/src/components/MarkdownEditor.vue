<template>
  <div class="markdown-editor">
    <div class="editor-toolbar">
      <div class="toolbar-group">
        <button class="toolbar-btn" @click="insertFormat('bold')" title="粗体 (Ctrl+B)">
          <strong>B</strong>
        </button>
        <button class="toolbar-btn" @click="insertFormat('italic')" title="斜体 (Ctrl+I)">
          <em>I</em>
        </button>
        <button class="toolbar-btn" @click="insertFormat('strikethrough')" title="删除线">
          <s>S</s>
        </button>
      </div>
      
      <div class="toolbar-divider"></div>
      
      <div class="toolbar-group">
        <button class="toolbar-btn" @click="insertFormat('h1')" title="一级标题">H1</button>
        <button class="toolbar-btn" @click="insertFormat('h2')" title="二级标题">H2</button>
        <button class="toolbar-btn" @click="insertFormat('h3')" title="三级标题">H3</button>
      </div>
      
      <div class="toolbar-divider"></div>
      
      <div class="toolbar-group">
        <button class="toolbar-btn" @click="insertFormat('ul')" title="无序列表">•</button>
        <button class="toolbar-btn" @click="insertFormat('ol')" title="有序列表">1.</button>
        <button class="toolbar-btn" @click="insertFormat('quote')" title="引用">❝</button>
        <button class="toolbar-btn" @click="insertFormat('code')" title="代码块">{ }</button>
      </div>
      
      <div class="toolbar-divider"></div>
      
      <div class="toolbar-group">
        <button class="toolbar-btn" @click="insertFormat('link')" title="链接">🔗</button>
        <button class="toolbar-btn" @click="insertFormat('image')" title="图片">🖼️</button>
      </div>
      
      <div class="toolbar-spacer"></div>
      
      <div class="toolbar-group">
        <span v-if="hasChanges" class="unsaved-indicator">● 未保存</span>
        <button 
          class="btn btn-sm btn-primary" 
          @click="save" 
          :disabled="saving || !hasChanges"
        >
          {{ saving ? '保存中...' : '💾 保存' }}
        </button>
      </div>
    </div>
    
    <div class="editor-content">
      <div class="editor-pane">
        <textarea
          ref="textareaRef"
          v-model="editContent"
          class="editor-textarea"
          @input="handleInput"
          @scroll="handleEditorScroll"
          @keydown="handleKeydown"
          placeholder="在此输入 Markdown 内容..."
        ></textarea>
      </div>
      
      <div class="preview-pane" ref="previewRef" @scroll="handlePreviewScroll">
        <div class="markdown-preview" v-html="renderedContent"></div>
      </div>
    </div>
    
    <div v-if="error" class="editor-error">{{ error }}</div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import { markedHighlight } from 'marked-highlight'
import { fileApi } from '@/services/api'

// 配置marked使用highlight.js
marked.use(markedHighlight({
  langPrefix: 'hljs language-',
  highlight(code, lang) {
    const language = hljs.getLanguage(lang) ? lang : 'plaintext'
    return hljs.highlight(code, { language }).value
  }
}))

const props = defineProps<{
  path: string
}>()

const emit = defineEmits<{
  (e: 'saved'): void
  (e: 'change', hasChanges: boolean): void
}>()

const textareaRef = ref<HTMLTextAreaElement | null>(null)
const previewRef = ref<HTMLElement | null>(null)

const originalContent = ref('')
const editContent = ref('')
const loading = ref(false)
const saving = ref(false)
const error = ref<string | null>(null)
const isScrolling = ref(false)

const hasChanges = computed(() => editContent.value !== originalContent.value)

const renderedContent = computed(() => {
  try {
    return marked(editContent.value)
  } catch {
    return '<p>渲染错误</p>'
  }
})

// 加载内容
async function loadContent() {
  if (!props.path) return
  
  loading.value = true
  error.value = null
  
  try {
    const content = await fileApi.getContent(props.path)
    originalContent.value = content.content
    editContent.value = content.content
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

// 保存
async function save() {
  if (!hasChanges.value) return
  
  saving.value = true
  error.value = null
  
  try {
    await fileApi.saveContent({
      path: props.path,
      content: editContent.value
    })
    originalContent.value = editContent.value
    emit('saved')
  } catch (e) {
    error.value = e instanceof Error ? e.message : '保存失败'
  } finally {
    saving.value = false
  }
}

function handleInput() {
  emit('change', hasChanges.value)
}

// 插入格式
function insertFormat(type: string) {
  const textarea = textareaRef.value
  if (!textarea) return
  
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const text = editContent.value
  const selected = text.substring(start, end)
  
  let insert = ''
  let cursorOffset = 0
  
  switch (type) {
    case 'bold':
      insert = `**${selected || '粗体文本'}**`
      cursorOffset = selected ? insert.length : 2
      break
    case 'italic':
      insert = `*${selected || '斜体文本'}*`
      cursorOffset = selected ? insert.length : 1
      break
    case 'strikethrough':
      insert = `~~${selected || '删除线文本'}~~`
      cursorOffset = selected ? insert.length : 2
      break
    case 'h1':
      insert = `# ${selected || '一级标题'}`
      cursorOffset = insert.length
      break
    case 'h2':
      insert = `## ${selected || '二级标题'}`
      cursorOffset = insert.length
      break
    case 'h3':
      insert = `### ${selected || '三级标题'}`
      cursorOffset = insert.length
      break
    case 'ul':
      insert = `- ${selected || '列表项'}`
      cursorOffset = insert.length
      break
    case 'ol':
      insert = `1. ${selected || '列表项'}`
      cursorOffset = insert.length
      break
    case 'quote':
      insert = `> ${selected || '引用文本'}`
      cursorOffset = insert.length
      break
    case 'code':
      insert = selected ? `\`\`\`\n${selected}\n\`\`\`` : '```\n代码\n```'
      cursorOffset = 4
      break
    case 'link':
      insert = `[${selected || '链接文本'}](url)`
      cursorOffset = selected ? insert.length - 1 : 1
      break
    case 'image':
      insert = `![${selected || '图片描述'}](url)`
      cursorOffset = selected ? insert.length - 1 : 2
      break
  }
  
  editContent.value = text.substring(0, start) + insert + text.substring(end)
  
  // 设置光标位置
  setTimeout(() => {
    textarea.focus()
    const newPos = start + cursorOffset
    textarea.setSelectionRange(newPos, newPos)
  }, 0)
  
  emit('change', hasChanges.value)
}

// 键盘快捷键
function handleKeydown(e: KeyboardEvent) {
  if (e.ctrlKey || e.metaKey) {
    switch (e.key.toLowerCase()) {
      case 'b':
        e.preventDefault()
        insertFormat('bold')
        break
      case 'i':
        e.preventDefault()
        insertFormat('italic')
        break
      case 's':
        e.preventDefault()
        save()
        break
    }
  }
}

// 滚动同步
function handleEditorScroll() {
  if (isScrolling.value) return
  isScrolling.value = true
  
  const textarea = textareaRef.value
  const preview = previewRef.value
  if (!textarea || !preview) return
  
  const ratio = textarea.scrollTop / (textarea.scrollHeight - textarea.clientHeight)
  preview.scrollTop = ratio * (preview.scrollHeight - preview.clientHeight)
  
  setTimeout(() => { isScrolling.value = false }, 50)
}

function handlePreviewScroll() {
  if (isScrolling.value) return
  isScrolling.value = true
  
  const textarea = textareaRef.value
  const preview = previewRef.value
  if (!textarea || !preview) return
  
  const ratio = preview.scrollTop / (preview.scrollHeight - preview.clientHeight)
  textarea.scrollTop = ratio * (textarea.scrollHeight - textarea.clientHeight)
  
  setTimeout(() => { isScrolling.value = false }, 50)
}

watch(() => props.path, () => {
  loadContent()
}, { immediate: true })

defineExpose({ save, hasChanges })
</script>

<style scoped>
.markdown-editor {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
}

.editor-toolbar {
  display: flex;
  align-items: center;
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.toolbar-group {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.toolbar-divider {
  width: 1px;
  height: 24px;
  background: var(--border-color);
  margin: 0 var(--spacing-xs);
}

.toolbar-spacer {
  flex: 1;
}

.toolbar-btn {
  width: 32px;
  height: 32px;
  border: 1px solid transparent;
  background: transparent;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 14px;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
}

.toolbar-btn:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
  border-color: var(--border-color);
}

.unsaved-indicator {
  color: var(--warning-color);
  font-size: 12px;
  margin-right: var(--spacing-sm);
}

.editor-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.editor-pane,
.preview-pane {
  flex: 1;
  overflow: auto;
}

.editor-pane {
  border-right: 1px solid var(--border-color);
}

.editor-textarea {
  width: 100%;
  height: 100%;
  padding: var(--spacing-md);
  border: none;
  resize: none;
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 14px;
  line-height: 1.6;
  outline: none;
  background: var(--bg-primary);
  color: var(--text-primary);
}

.preview-pane {
  background: var(--bg-primary);
}

.markdown-preview {
  padding: var(--spacing-md);
  line-height: 1.6;
}

.markdown-preview :deep(h1),
.markdown-preview :deep(h2),
.markdown-preview :deep(h3) {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.markdown-preview :deep(h1) { font-size: 2em; border-bottom: 1px solid var(--border-color); padding-bottom: 8px; }
.markdown-preview :deep(h2) { font-size: 1.5em; }
.markdown-preview :deep(h3) { font-size: 1.25em; }

.markdown-preview :deep(p) {
  margin-bottom: 16px;
}

.markdown-preview :deep(code) {
  background-color: var(--bg-tertiary);
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  font-family: 'Fira Code', monospace;
  font-size: 0.9em;
}

.markdown-preview :deep(pre) {
  background-color: var(--bg-tertiary);
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  overflow-x: auto;
  margin-bottom: 16px;
}

.markdown-preview :deep(pre code) {
  background: none;
  padding: 0;
}

.markdown-preview :deep(ul),
.markdown-preview :deep(ol) {
  margin-bottom: 16px;
  padding-left: 24px;
}

.markdown-preview :deep(blockquote) {
  border-left: 4px solid var(--primary-color);
  padding-left: 16px;
  color: var(--text-secondary);
  margin: 16px 0;
  background: var(--bg-secondary);
  padding: var(--spacing-md);
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
}

.markdown-preview :deep(a) {
  color: var(--primary-color);
  text-decoration: none;
}

.markdown-preview :deep(a:hover) {
  text-decoration: underline;
}

.markdown-preview :deep(img) {
  max-width: 100%;
  border-radius: var(--radius-md);
}

.markdown-preview :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 16px;
}

.markdown-preview :deep(th),
.markdown-preview :deep(td) {
  border: 1px solid var(--border-color);
  padding: var(--spacing-sm);
  text-align: left;
}

.markdown-preview :deep(th) {
  background: var(--bg-secondary);
}

.editor-error {
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--error-light);
  color: var(--error-color);
  font-size: 13px;
}

@media (max-width: 768px) {
  .editor-content {
    flex-direction: column;
  }
  
  .editor-pane {
    border-right: none;
    border-bottom: 1px solid var(--border-color);
  }
}
</style>
