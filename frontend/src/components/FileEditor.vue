<template>
  <div class="file-editor">
    <div class="editor-header">
      <span class="file-path">{{ path }}</span>
      <span v-if="hasChanges" class="unsaved-indicator">● 未保存</span>
      <button class="btn btn-primary" @click="save" :disabled="saving || !hasChanges">
        {{ saving ? '保存中...' : '保存' }}
      </button>
    </div>
    <div class="editor-content">
      <textarea
        v-model="editContent"
        class="editor-textarea"
        @input="handleInput"
        :placeholder="loading ? '加载中...' : ''"
        :disabled="loading"
      ></textarea>
    </div>
    <div v-if="error" class="editor-error">{{ error }}</div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { fileApi } from '@/services/api'

const props = defineProps<{
  path: string
}>()

const emit = defineEmits<{
  saved: []
  change: [hasChanges: boolean]
}>()

const originalContent = ref('')
const editContent = ref('')
const loading = ref(false)
const saving = ref(false)
const error = ref<string | null>(null)

const hasChanges = computed(() => editContent.value !== originalContent.value)

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

function checkUnsavedChanges(): boolean {
  return hasChanges.value
}

watch(() => props.path, () => {
  loadContent()
}, { immediate: true })

defineExpose({ save, checkUnsavedChanges, hasChanges })
</script>

<style scoped>
.file-editor {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: white;
}

.editor-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-bottom: 1px solid #e0e0e0;
  background: #fafafa;
}

.file-path {
  flex: 1;
  font-family: monospace;
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unsaved-indicator {
  color: #ff9800;
  font-size: 12px;
}

.editor-content {
  flex: 1;
  overflow: hidden;
}

.editor-textarea {
  width: 100%;
  height: 100%;
  padding: 16px;
  border: none;
  resize: none;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  line-height: 1.5;
  outline: none;
}

.editor-error {
  padding: 8px 12px;
  background: #ffebee;
  color: #c62828;
  font-size: 13px;
}
</style>
