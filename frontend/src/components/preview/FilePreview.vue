<template>
  <div class="file-preview">
    <div v-if="loading" class="preview-loading">加载中...</div>
    <div v-else-if="error" class="preview-error">{{ error }}</div>
    <div v-else-if="!content" class="preview-empty">
      <p>选择一个文件进行预览</p>
    </div>
    <template v-else>
      <MarkdownPreview v-if="extension === 'md'" :content="content.content" />
      <HtmlPreview v-else-if="extension === 'html' || extension === 'htm'" :content="content.content" />
      <PdfPreview v-else-if="extension === 'pdf'" :path="path" />
      <TextPreview v-else-if="isTextFile" :content="content.content" />
      <div v-else class="preview-unsupported">
        <p>📄 不支持预览此文件类型</p>
        <p class="file-info">{{ path }}</p>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import type { FileContent } from '@/types'
import { fileApi } from '@/services/api'
import MarkdownPreview from './MarkdownPreview.vue'
import TextPreview from './TextPreview.vue'
import HtmlPreview from './HtmlPreview.vue'
import PdfPreview from './PdfPreview.vue'

const props = defineProps<{
  path: string
  extension?: string
}>()

const content = ref<FileContent | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

const textExtensions = ['txt', 'md', 'html', 'htm', 'css', 'js', 'ts', 'json', 'xml', 'yaml', 'yml', 'java', 'py', 'sql']
const isTextFile = computed(() => props.extension && textExtensions.includes(props.extension.toLowerCase()))

async function loadContent() {
  if (!props.path) {
    content.value = null
    return
  }
  
  loading.value = true
  error.value = null
  
  try {
    content.value = await fileApi.getContent(props.path)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
    content.value = null
  } finally {
    loading.value = false
  }
}

watch(() => props.path, () => {
  if (props.path) {
    loadContent()
  } else {
    content.value = null
  }
}, { immediate: true })

defineExpose({ refresh: loadContent })
</script>

<style scoped>
.file-preview {
  height: 100%;
  overflow: auto;
  background: white;
}

.preview-loading,
.preview-error,
.preview-empty,
.preview-unsupported {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #666;
}

.preview-error {
  color: #d32f2f;
}

.file-info {
  font-family: monospace;
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}
</style>
