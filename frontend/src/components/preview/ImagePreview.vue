<template>
  <div class="image-preview">
    <div class="image-toolbar">
      <div class="toolbar-group">
        <button class="toolbar-btn" @click="zoomOut" :disabled="scale <= 0.1" title="缩小 (-)">
          <span class="icon">−</span>
        </button>
        <span class="zoom-info">{{ Math.round(scale * 100) }}%</span>
        <button class="toolbar-btn" @click="zoomIn" :disabled="scale >= 10" title="放大 (+)">
          <span class="icon">+</span>
        </button>
      </div>
      
      <div class="toolbar-divider"></div>
      
      <div class="toolbar-group">
        <button class="toolbar-btn" @click="resetView" title="重置视图">
          <span class="icon">⟲</span>
        </button>
        <button class="toolbar-btn" @click="fitToWindow" title="适应窗口">
          <span class="icon">⊡</span>
        </button>
        <button class="toolbar-btn" @click="actualSize" title="实际大小 (1:1)">
          <span class="icon">1:1</span>
        </button>
      </div>
      
      <div class="toolbar-divider"></div>
      
      <div class="toolbar-group">
        <a :href="imageUrl" download class="toolbar-btn link-btn" title="下载图片">
          <span class="icon">↓</span>
        </a>
        <a :href="imageUrl" target="_blank" class="toolbar-btn link-btn" title="新窗口打开">
          <span class="icon">↗</span>
        </a>
      </div>
    </div>
    
    <div 
      class="image-container" 
      ref="containerRef"
      @wheel.prevent="handleWheel"
      @mousedown="startDrag"
      @mousemove="onDrag"
      @mouseup="endDrag"
      @mouseleave="endDrag"
      :class="{ dragging: isDragging, grabbable: imageLoaded }"
    >
      <div v-if="loading && !imageLoaded" class="image-loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      
      <div v-if="error" class="image-error">
        <div class="error-icon">🖼️</div>
        <p class="error-title">图片加载失败</p>
        <p class="error-message">{{ error }}</p>
      </div>
      
      <div 
        v-show="imageLoaded && !error"
        class="image-wrapper"
        :style="wrapperStyle"
      >
        <img 
          ref="imageRef"
          :src="imageUrl"
          class="preview-image"
          @load="handleImageLoad"
          @error="handleImageError"
          draggable="false"
        />
      </div>
    </div>
    
    <div class="image-info" v-if="imageInfo">
      <span class="info-item">
        <span class="info-label">尺寸:</span>
        {{ imageInfo.width }} × {{ imageInfo.height }}
      </span>
      <span class="info-divider">|</span>
      <span class="info-item">
        <span class="info-label">文件:</span>
        {{ fileName }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps<{
  path: string
}>()

const containerRef = ref<HTMLElement | null>(null)
const imageRef = ref<HTMLImageElement | null>(null)

const loading = ref(true)
const imageLoaded = ref(false)
const error = ref<string | null>(null)
const scale = ref(1)
const translateX = ref(0)
const translateY = ref(0)
const imageInfo = ref<{ width: number; height: number } | null>(null)

// 拖动状态
const isDragging = ref(false)
const dragStartX = ref(0)
const dragStartY = ref(0)
const dragStartTranslateX = ref(0)
const dragStartTranslateY = ref(0)

const imageUrl = computed(() => {
  return `/api/files/download?path=${encodeURIComponent(props.path)}`
})

const fileName = computed(() => {
  const parts = props.path.split('/')
  return parts[parts.length - 1] || props.path
})

const wrapperStyle = computed(() => ({
  transform: `translate(${translateX.value}px, ${translateY.value}px) scale(${scale.value})`,
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
  error.value = '无法加载图片，请检查文件是否存在'
}

function zoomIn() {
  if (scale.value < 10) {
    scale.value = Math.min(10, scale.value * 1.25)
  }
}

function zoomOut() {
  if (scale.value > 0.1) {
    scale.value = Math.max(0.1, scale.value / 1.25)
  }
}

function resetView() {
  scale.value = 1
  translateX.value = 0
  translateY.value = 0
}

function actualSize() {
  scale.value = 1
  translateX.value = 0
  translateY.value = 0
}

function fitToWindow() {
  if (!containerRef.value || !imageInfo.value) return
  
  const containerWidth = containerRef.value.clientWidth - 60
  const containerHeight = containerRef.value.clientHeight - 60
  
  const widthRatio = containerWidth / imageInfo.value.width
  const heightRatio = containerHeight / imageInfo.value.height
  
  scale.value = Math.min(widthRatio, heightRatio, 1)
  translateX.value = 0
  translateY.value = 0
}

function handleWheel(e: WheelEvent) {
  const delta = e.deltaY > 0 ? 0.9 : 1.1
  const newScale = Math.max(0.1, Math.min(10, scale.value * delta))
  scale.value = newScale
}

// 拖动功能
function startDrag(e: MouseEvent) {
  if (!imageLoaded.value) return
  isDragging.value = true
  dragStartX.value = e.clientX
  dragStartY.value = e.clientY
  dragStartTranslateX.value = translateX.value
  dragStartTranslateY.value = translateY.value
}

function onDrag(e: MouseEvent) {
  if (!isDragging.value) return
  const deltaX = e.clientX - dragStartX.value
  const deltaY = e.clientY - dragStartY.value
  translateX.value = dragStartTranslateX.value + deltaX
  translateY.value = dragStartTranslateY.value + deltaY
}

function endDrag() {
  isDragging.value = false
}

// 键盘快捷键
function handleKeydown(e: KeyboardEvent) {
  if (e.key === '+' || e.key === '=') {
    zoomIn()
  } else if (e.key === '-') {
    zoomOut()
  } else if (e.key === '0') {
    resetView()
  }
}

onMounted(() => {
  loading.value = true
  imageLoaded.value = false
  error.value = null
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.image-preview {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--bg-secondary);
}

.image-toolbar {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
  gap: 12px;
  flex-wrap: wrap;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.toolbar-group {
  display: flex;
  align-items: center;
  gap: 4px;
}

.toolbar-divider {
  width: 1px;
  height: 20px;
  background: var(--border-color);
  margin: 0 4px;
}

.toolbar-btn {
  width: 32px;
  height: 32px;
  border: 1px solid var(--border-color);
  background: var(--bg-primary);
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: var(--text-secondary);
  transition: all 0.15s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toolbar-btn:hover:not(:disabled) {
  background: var(--bg-tertiary);
  color: var(--text-primary);
  border-color: var(--primary-color);
}

.toolbar-btn:active:not(:disabled) {
  transform: scale(0.95);
}

.toolbar-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.toolbar-btn .icon {
  font-weight: 600;
  font-size: 14px;
}

.link-btn {
  text-decoration: none;
}

.zoom-info {
  min-width: 52px;
  text-align: center;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  font-family: monospace;
}

.image-container {
  flex: 1;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  /* 美化的深色渐变背景 */
  background: 
    radial-gradient(circle at 50% 50%, #2a2a3a 0%, #1a1a2e 100%);
}

/* 添加微妙的网格图案 */
.image-container::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
  background-size: 20px 20px;
  pointer-events: none;
}

.image-container.grabbable {
  cursor: grab;
}

.image-container.dragging {
  cursor: grabbing;
}

.image-loading,
.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.7);
  background: rgba(0, 0, 0, 0.3);
  padding: 32px 48px;
  border-radius: 12px;
  backdrop-filter: blur(8px);
  z-index: 1;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(255, 255, 255, 0.2);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.image-error {
  color: #ff6b6b;
}

.error-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.8;
}

.error-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
}

.error-message {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
}

.image-wrapper {
  position: relative;
  transform-origin: center center;
  transition: transform 0.1s ease-out;
  z-index: 1;
}

.preview-image {
  display: block;
  max-width: none;
  max-height: none;
  box-shadow: 
    0 4px 20px rgba(0, 0, 0, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  user-select: none;
}

.image-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 8px 16px;
  background: var(--bg-primary);
  border-top: 1px solid var(--border-color);
  font-size: 12px;
  color: var(--text-muted);
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.info-label {
  color: var(--text-muted);
  opacity: 0.7;
}

.info-divider {
  color: var(--border-color);
}
</style>
