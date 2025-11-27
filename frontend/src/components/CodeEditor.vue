<template>
  <div class="code-editor">
    <div class="editor-header">
      <div class="editor-info">
        <span class="file-path">{{ path }}</span>
        <span class="language-badge">{{ language }}</span>
      </div>
      <div class="editor-actions">
        <span v-if="hasChanges" class="unsaved-indicator">● 未保存</span>
        <button class="btn btn-sm" @click="formatCode" title="格式化代码">
          🔧 格式化
        </button>
        <button 
          class="btn btn-sm btn-primary" 
          @click="save" 
          :disabled="saving || !hasChanges"
        >
          {{ saving ? '保存中...' : '💾 保存' }}
        </button>
      </div>
    </div>
    
    <div class="editor-container" ref="editorContainer"></div>
    
    <div v-if="error" class="editor-error">
      <span>⚠️ {{ error }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted, onUnmounted } from 'vue'
import * as monaco from 'monaco-editor'
import { fileApi } from '@/services/api'
import { getLanguageByExtension } from '@/utils/languageMap'

const props = defineProps<{
  path: string
  extension?: string
  readOnly?: boolean
}>()

const emit = defineEmits<{
  (e: 'saved'): void
  (e: 'change', hasChanges: boolean): void
}>()

const editorContainer = ref<HTMLElement | null>(null)
let editor: monaco.editor.IStandaloneCodeEditor | null = null

const originalContent = ref('')
const currentContent = ref('')
const loading = ref(false)
const saving = ref(false)
const error = ref<string | null>(null)

const language = computed(() => getLanguageByExtension(props.extension))
const hasChanges = computed(() => currentContent.value !== originalContent.value)

// 初始化编辑器
function initEditor() {
  if (!editorContainer.value) return
  
  editor = monaco.editor.create(editorContainer.value, {
    value: currentContent.value,
    language: language.value,
    theme: 'vs',
    readOnly: props.readOnly,
    automaticLayout: true,
    minimap: { enabled: true },
    lineNumbers: 'on',
    renderLineHighlight: 'line',
    scrollBeyondLastLine: false,
    fontSize: 14,
    fontFamily: "'Fira Code', 'Consolas', 'Monaco', monospace",
    tabSize: 2,
    insertSpaces: true,
    wordWrap: 'on',
    bracketPairColorization: { enabled: true },
    matchBrackets: 'always',
    folding: true,
    smoothScrolling: true,
    cursorBlinking: 'smooth',
    cursorSmoothCaretAnimation: 'on',
    padding: { top: 16, bottom: 16 }
  })
  
  // 监听内容变化
  editor.onDidChangeModelContent(() => {
    currentContent.value = editor?.getValue() || ''
    emit('change', hasChanges.value)
  })
  
  // 添加保存快捷键
  editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.KeyS, () => {
    save()
  })
}

// 加载文件内容
async function loadContent() {
  if (!props.path) return
  
  loading.value = true
  error.value = null
  
  try {
    const content = await fileApi.getContent(props.path)
    originalContent.value = content.content
    currentContent.value = content.content
    
    if (editor) {
      editor.setValue(content.content)
      monaco.editor.setModelLanguage(editor.getModel()!, language.value)
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

// 保存文件
async function save() {
  if (!hasChanges.value) return
  
  saving.value = true
  error.value = null
  
  try {
    await fileApi.saveContent({
      path: props.path,
      content: currentContent.value
    })
    originalContent.value = currentContent.value
    emit('saved')
  } catch (e) {
    error.value = e instanceof Error ? e.message : '保存失败'
  } finally {
    saving.value = false
  }
}

// 格式化代码
function formatCode() {
  editor?.getAction('editor.action.formatDocument')?.run()
}

// 监听路径变化
watch(() => props.path, () => {
  loadContent()
})

// 监听语言变化
watch(language, (newLang) => {
  if (editor) {
    monaco.editor.setModelLanguage(editor.getModel()!, newLang)
  }
})

onMounted(() => {
  initEditor()
  loadContent()
})

onUnmounted(() => {
  editor?.dispose()
})

defineExpose({ save, hasChanges })
</script>

<style scoped>
.code-editor {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
}

.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.editor-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  min-width: 0;
  flex: 1;
}

.file-path {
  font-family: monospace;
  font-size: 13px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.language-badge {
  padding: 2px 8px;
  background: var(--primary-light);
  color: var(--primary-color);
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 500;
  text-transform: uppercase;
  flex-shrink: 0;
}

.editor-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.unsaved-indicator {
  color: var(--warning-color);
  font-size: 12px;
  font-weight: 500;
}

.editor-container {
  flex: 1;
  overflow: hidden;
}

.editor-error {
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--error-light);
  color: var(--error-color);
  font-size: 13px;
}
</style>
