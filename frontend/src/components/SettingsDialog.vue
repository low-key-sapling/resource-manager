<template>
  <Teleport to="body">
    <div v-if="visible" class="dialog-overlay" @click.self="close">
      <div class="dialog">
        <div class="dialog-header">
          <h3>⚙️ 设置</h3>
          <button class="close-btn" @click="close">✕</button>
        </div>
        
        <div class="dialog-body">
          <div class="form-group">
            <label class="form-label">根目录路径</label>
            <div class="input-group">
              <input 
                type="text" 
                v-model="rootPath" 
                class="input"
                placeholder="输入根目录路径，如 D:\WorkNotes"
              />
            </div>
            <p class="form-hint">设置文件管理器的根目录，所有文件操作将在此目录下进行</p>
          </div>
          
          <div v-if="currentPath" class="current-path">
            <span class="label">当前根目录：</span>
            <code>{{ currentPath }}</code>
          </div>
          
          <div v-if="error" class="error-message">
            ⚠️ {{ error }}
          </div>
          
          <div v-if="success" class="success-message">
            ✅ {{ success }}
          </div>
        </div>
        
        <div class="dialog-footer">
          <button class="btn" @click="close">取消</button>
          <button class="btn btn-primary" @click="save" :disabled="saving">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { fileApi } from '@/services/api'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'saved'): void
}>()

const rootPath = ref('')
const currentPath = ref('')
const saving = ref(false)
const error = ref('')
const success = ref('')

async function loadCurrentPath() {
  try {
    currentPath.value = await fileApi.getRootPath()
    rootPath.value = currentPath.value
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  }
}

async function save() {
  if (!rootPath.value.trim()) {
    error.value = '请输入根目录路径'
    return
  }
  
  saving.value = true
  error.value = ''
  success.value = ''
  
  try {
    await fileApi.setRootPath(rootPath.value.trim())
    currentPath.value = rootPath.value.trim()
    success.value = '根目录设置成功'
    emit('saved')
    
    setTimeout(() => {
      close()
    }, 1000)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '设置失败'
  } finally {
    saving.value = false
  }
}

function close() {
  emit('update:visible', false)
  error.value = ''
  success.value = ''
}

watch(() => props.visible, (newVal) => {
  if (newVal) {
    loadCurrentPath()
  }
})
</script>

<style scoped>
.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn var(--transition-fast);
}

.dialog {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
  width: 90%;
  max-width: 500px;
  animation: scaleIn var(--transition-fast);
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md) var(--spacing-lg);
  border-bottom: 1px solid var(--border-color);
}

.dialog-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.close-btn {
  background: transparent;
  border: none;
  font-size: 18px;
  color: var(--text-muted);
  cursor: pointer;
  padding: var(--spacing-xs);
  border-radius: var(--radius-sm);
}

.close-btn:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.dialog-body {
  padding: var(--spacing-lg);
}

.form-group {
  margin-bottom: var(--spacing-md);
}

.form-label {
  display: block;
  font-weight: 500;
  margin-bottom: var(--spacing-sm);
  color: var(--text-primary);
}

.input-group {
  display: flex;
  gap: var(--spacing-sm);
}

.input {
  flex: 1;
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: 14px;
  outline: none;
  transition: border-color var(--transition-fast);
}

.input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--primary-light);
}

.form-hint {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: var(--spacing-xs);
}

.current-path {
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  font-size: 13px;
  margin-bottom: var(--spacing-md);
}

.current-path .label {
  color: var(--text-secondary);
}

.current-path code {
  font-family: monospace;
  color: var(--primary-color);
}

.error-message {
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--error-light);
  color: var(--error-color);
  border-radius: var(--radius-md);
  font-size: 13px;
  margin-bottom: var(--spacing-md);
}

.success-message {
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--success-light);
  color: var(--success-color);
  border-radius: var(--radius-md);
  font-size: 13px;
  margin-bottom: var(--spacing-md);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-sm);
  padding: var(--spacing-md) var(--spacing-lg);
  border-top: 1px solid var(--border-color);
}
</style>
