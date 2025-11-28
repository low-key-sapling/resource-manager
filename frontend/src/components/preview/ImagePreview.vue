<template>
  <div class="image-preview">
    <div class="image-toolbar">
      <div class="toolbar-group">
        <button class="toolbar-btn" @click="zoomOut" :disabled="scale <= 0.25" title="缩小">
          ➖
        </button>
        <span class="zoom-info">{{ Math.round(scale * 100) }}%</span>
        <button class="toolbar-btn" @click="zoomIn" :disabled="scale >= 5" title="放大">
          ➕
        </button>
        <button class="toolbar-btn" @click="resetZoom" title="重置">
          🔄
        </button>
        <button class="toolbar-btn" @click="fitToWindow" title="适应窗口">
          ⊡
        </button>
      </div>
      
      <div class="toolbar-divider"></div>
      
      <div class="toolbar-group">
        <a :href="imageUrl" download class="btn btn-sm" title="下载图片">
          ⬇️ 下载
        </a>
        <a :href="imageUrl" target="_blank" class="btn btn-sm" title="新窗口打开">
          🔗 新窗口
        </a>
      </div>
    </div>
    
    <div 
      class="image-container" 
      ref="containerRef"
      @wheel.prevent="handleWheel"
    >
      <div v-if="loading && !imageLoaded" class="image-loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      
      <div v-if="error" class="image-error">
        <p>🖼️ 图片加载失败</p>
        <p class="error-message">{{ error }}</p>
      </div>
      
      <img 
        v-show="imageLoaded && !error"
        ref="imageRef"
        :src="imageUrl"
        :style="imageStyle"
        class="preview-image"
        @load="handleImageLoad"
        @error="handleImageError"
        draggable="false"
      />
    </div>
    
    <div class="image-info" v-if="imageInfo">
      <span>{{ imageInfo.width }} × {{ imageInfo.height }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

const props = defineProps<{
  path: string
}>()

const containerRef = ref<HTMLElement | null>(null)
const imageRef = ref<HTMLImageElement | null>(null)

const loading = ref(true)
const imageLoaded = ref(false)
const error = ref<string | null>(null)
const scale = ref(1)
const imageInfo = ref<{ width: number; height: number } | null>(null)

const imageUrl = computed(() => {
  return `/api/files/download?path=${encodeURIComponent(props.path)}`
})

const imageStyle = computed(() => ({
  transform: `scale(${scale.value})`,
  transformOrigin: 'center center'
}))

function handleImageLoad(e: Event) {
  loading.value = false
  imageLoaded.value = true
  const img = e.target as HTMLImageElement
  imageInfo.value = {
    width: img.naturalWidth,
    height: img.naturalHeight
  }
  fitToWindow()
}

function handleImageError() {
  loading.value = false
  error.value = '无法加载图片'
}

function zoomIn() {
  if (scale.value < 5) {
    scale.value = Math.min(5, scale.value * 1.25)
  }
}

function zoomOut() {
  if (scale.value > 0.25) {
    scale.value = Math.max(0.25, scale.value / 1.25)
  }
}

function resetZoom() {
  scale.value = 1
}

function fitToWindow() {
  if (!containerRef.value || !imageInfo.value) return
  
  const containerWidth = containerRef.value.clientWidth - 40
  const containerHeight = containerRef.value.clientHeight - 40
  
  const widthRatio = containerWidth / imageInfo.value.width
  const heightRatio = containerHeight / imageInfo.value.height
  
  scale.value = Math.min(widthRatio, heightRatio, 1)
}

function handleWheel(e: WheelEvent) {
  if (e.deltaY < 0) {
    zoomIn()
  } else {
    zoomOut()
  }
}

onMounted(() => {
  loading.value = true
  imageLoaded.value = false
  error.value = null
})
</script>

<style scoped>
.image-preview {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--bg-tertiary);
}

.image-toolbar {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
  gap: var(--spacing-md);
  flex-wrap: wrap;
}

.toolbar-group {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.toolbar-divider {
  width: 1px;
  height: 24px;
  background: var(--border-color);
}

.toolbar-btn {
  width: 32px;
  height: 32px;
  border: 1px solid var(--border-color);
  background: var(--bg-primary);
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 14px;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
}

.toolbar-btn:hover:not(:disabled) {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.toolbar-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.zoom-info {
  min-width: 50px;
  text-align: center;
  font-size: 14px;
  color: var(--text-secondary);
}

.image-container {
  flex: 1;
  overflow: auto;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-md);
  background: 
    linear-gradient(45deg, #e0e0e0 25%, transparent 25%),
    linear-gradient(-45deg, #e0e0e0 25%, transparent 25%),
    linear-gradient(45deg, transparent 75%, #e0e0e0 75%),
    linear-gradient(-45deg, transparent 75%, #e0e0e0 75%);
  background-size: 20px 20px;
  background-position: 0 0, 0 10px, 10px -10px, -10px 0px;
  background-color: #f0f0f0;
}

.image-loading,
.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  background: var(--bg-primary);
  padding: var(--spacing-xl);
  border-radius: var(--radius-lg);
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

.image-error {
  color: var(--error-color);
}

.error-message {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: var(--spacing-sm);
}

.preview-image {
  max-width: none;
  max-height: none;
  box-shadow: var(--shadow-lg);
  transition: transform var(--transition-fast);
}

.image-info {
  padding: var(--spacing-xs) var(--spacing-md);
  background: var(--bg-primary);
  border-top: 1px solid var(--border-color);
  font-size: 12px;
  color: var(--text-muted);
  text-align: center;
}
</style>
