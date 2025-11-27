<template>
  <div v-if="visible" class="dialog-overlay" @click.self="close">
    <div class="dialog">
      <div class="dialog-header">
        <h3>{{ isDirectory ? '新建目录' : '新建文件' }}</h3>
        <button class="close-btn" @click="close">×</button>
      </div>
      <div class="dialog-body">
        <div class="form-group">
          <label>{{ isDirectory ? '目录名称' : '文件名称' }}</label>
          <input
            v-model="name"
            type="text"
            :placeholder="isDirectory ? '请输入目录名称' : '请输入文件名称（含扩展名）'"
            @keyup.enter="submit"
            ref="inputRef"
          />
          <span v-if="error" class="error-text">{{ error }}</span>
        </div>
      </div>
      <div class="dialog-footer">
        <button class="btn" @click="close">取消</button>
        <button class="btn btn-primary" @click="submit" :disabled="!name.trim() || loading">
          {{ loading ? '创建中...' : '创建' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { fileApi } from '@/services/api'

const props = defineProps<{
  visible: boolean
  isDirectory: boolean
  parentPath: string
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  created: []
}>()

const name = ref('')
const error = ref<string | null>(null)
const loading = ref(false)
const inputRef = ref<HTMLInputElement | null>(null)

function validateName(value: string): string | null {
  if (!value.trim()) {
    return '名称不能为空'
  }
  if (/[<>:"|?*\\\/]/.test(value)) {
    return '名称包含非法字符'
  }
  if (!props.isDirectory && !value.includes('.')) {
    return '文件名需要包含扩展名'
  }
  return null
}

async function submit() {
  const validationError = validateName(name.value)
  if (validationError) {
    error.value = validationError
    return
  }
  
  loading.value = true
  error.value = null
  
  const path = props.parentPath === '/' 
    ? `/${name.value}` 
    : `${props.parentPath}/${name.value}`
  
  try {
    if (props.isDirectory) {
      await fileApi.createDirectory(path)
    } else {
      await fileApi.createFile({ path, content: '' })
    }
    emit('created')
    close()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '创建失败'
  } finally {
    loading.value = false
  }
}

function close() {
  name.value = ''
  error.value = null
  emit('update:visible', false)
}

watch(() => props.visible, (val) => {
  if (val) {
    nextTick(() => inputRef.value?.focus())
  }
})
</script>

<style scoped>
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog {
  background: white;
  border-radius: 8px;
  width: 400px;
  max-width: 90%;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #e0e0e0;
}

.dialog-header h3 {
  margin: 0;
  font-size: 16px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}

.dialog-body {
  padding: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #333;
}

.form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-group input:focus {
  outline: none;
  border-color: #1976d2;
}

.error-text {
  display: block;
  margin-top: 6px;
  color: #d32f2f;
  font-size: 12px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 16px 20px;
  border-top: 1px solid #e0e0e0;
}
</style>
