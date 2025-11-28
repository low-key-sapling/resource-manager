<template>
  <div class="file-preview">
    <div v-if="loading" class="preview-loading">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>
    <div v-else-if="error" class="preview-error">
      <p>⚠️ {{ error }}</p>
    </div>
    <div v-else-if="isPdf" class="pdf-wrapper">
      <PdfPreview :path="path" />
    </div>
    <div v-else-if="isImage" class="image-wrapper">
      <ImagePreview 
        :path="path" 
        :sibling-images="siblingImages || []"
        @navigate="(p: string) => emit('navigateImage', p)"
      />
    </div>
    <div v-else-if="!content" class="preview-empty">
      <div class="empty-icon">📄</div>
      <p>选择一个文件进行预览</p>
    </div>
    <template v-else>
      <MarkdownPreview v-if="isMarkdown" :content="content.content" />
      <HtmlPreview v-else-if="isHtml" :content="content.content" />
      <TextPreview v-else-if="isTextFile" :content="content.content" />
      <div v-else class="preview-unsupported">
        <div class="unsupported-icon">📄</div>
        <p>不支持预览此文件类型</p>
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
import ImagePreview from './ImagePreview.vue'

const props = defineProps<{
  path: string
  extension?: string
  siblingImages?: string[]
}>()

const emit = defineEmits<{
  (e: 'navigateImage', path: string): void
}>()

const content = ref<FileContent | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

const textExtensions = ['txt', 'css', 'js', 'ts', 'json', 'xml', 'yaml', 'yml', 'java', 'py', 'sql', 'sh', 'bat', 'ini', 'conf', 'log', 'vue', 'jsx', 'tsx']
const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg', 'ico']
const ext = computed(() => props.extension?.toLowerCase())
const isTextFile = computed(() => ext.value && textExtensions.includes(ext.value))
const isMarkdown = computed(() => ext.value === 'md' || ext.value === 'markdown')
const isHtml = computed(() => ext.value === 'html' || ext.value === 'htm')
const isPdf = computed(() => ext.value === 'pdf')
const isImage = computed(() => ext.value && imageExtensions.includes(ext.value))

async function loadContent() {
  if (!props.path) {
    content.value = null
    return
  }
  
  // 没有扩展名的可能是目录，不加载
  if (!props.extension) {
    loading.value = false
    error.value = null
    content.value = null
    return
  }
  
  // PDF和图片文件不需要加载文本内容，直接由专用组件处理
  if (isPdf.value || isImage.value) {
    loading.value = false
    error.value = null
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

watch([() => props.path, () => props.extension], () => {
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
  background: var(--bg-primary);
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
  color: var(--text-secondary);
  padding: var(--spacing-xl);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--border-color);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: var(--spacing-md);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.preview-error {
  color: var(--error-color);
}

.empty-icon,
.unsupported-icon {
  font-size: 48px;
  margin-bottom: var(--spacing-md);
  opacity: 0.5;
}

.file-info {
  font-family: monospace;
  font-size: 12px;
  color: var(--text-muted);
  margin-top: var(--spacing-sm);
}

.pdf-wrapper,
.image-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
}
</style>
