<template>
  <div class="image-preview">
    <div class="image-toolbar">
      <!-- 图片导航 -->
      <div class="toolbar-group nav-group" v-if="siblingImages.length > 1">
        <button class="toolbar-btn nav-btn" @click="prevImage" :disabled="!hasPrev" title="上一张 (←)">
          <span class="icon">‹</span>
        </button>
        <span class="nav-info">{{ currentIndex + 1 }} / {{ siblingImages.length }}</span>
        <button class="toolbar-btn nav-btn" @click="nextImage" :disabled="!hasNext" title="下一张 (→)">
          <span class="icon">›</span>
        </button>
      </div>
      <div class="toolbar-divider" v-if="siblingImages.length > 1"></div>
      <!-- 缩放控制 -->
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
      <!-- 视图控制 -->
      <div class="toolbar-group">
        <button class="toolbar-btn" @click="resetView" title="重置视图"><span class="icon">⟲</span></button>
        <button class="toolbar-btn" @click="fitToWindow" title="适应窗口"><span class="icon">⊡</span></button>
        <button class="toolbar-btn" @click="actualSize" title="实际大小 (1:1)"><span class="icon">1:1</span></button>
        <button class="toolbar-btn" @click="openLightbox" title="全屏预览 (Space)"><span class="icon">⛶</span></button>
      </div>
      <div class="toolbar-divider"></div>
      <!-- 下载和新窗口 -->
      <div class="toolbar-group">
        <a :href="imageUrl" download class="toolbar-btn link-btn" title="下载图片"><span class="icon">↓</span></a>
        <a :href="imageUrl" target="_blank" class="toolbar-btn link-btn" title="新窗口打开"><span class="icon">↗</span></a>
      </div>
    </div>
    
    <div class="image-container" ref="containerRef" @wheel.prevent="handleWheel" @mousedown="startDrag"
      @mousemove="onDrag" @mouseup="endDrag" @mouseleave="endDrag"
      :class="{ dragging: isDragging, grabbable: imageLoaded }">
      <button v-if="siblingImages.length > 1 && hasPrev" class="nav-arrow nav-arrow-left" @click="prevImage">‹</button>
      <div v-if="loading && !imageLoaded" class="image-loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      <div v-if="error" class="image-error">
        <div class="error-icon">🖼️</div>
        <p class="error-title">图片加载失败</p>
        <p class="error-message">{{ error }}</p>
      </div>
      <div v-show="imageLoaded && !error" class="image-wrapper" :style="wrapperStyle" @click="handleImageClick">
        <img ref="imageRef" :src="imageUrl" class="preview-image" @load="handleImageLoad" @error="handleImageError" draggable="false" />
      </div>
      <button v-if="siblingImages.length > 1 && hasNext" class="nav-arrow nav-arrow-right" @click="nextImage">›</button>
    </div>
    
    <div class="image-info" v-if="imageInfo">
      <span class="info-item"><span class="info-label">尺寸:</span> {{ imageInfo.width }} × {{ imageInfo.height }}</span>
      <span class="info-divider">|</span>
      <span class="info-item"><span class="info-label">文件:</span> {{ fileName }}</span>
      <span class="info-divider">|</span>
      <span class="info-item hint">点击图片全屏预览</span>
    </div>

    <!-- 全屏灯箱 -->
    <Teleport to="body">
      <Transition name="lightbox">
        <div v-if="lightboxOpen" class="lightbox-overlay" @click.self="closeLightbox">
          <div class="lightbox-header">
            <span class="lightbox-title">{{ fileName }}</span>
            <div class="lightbox-actions">
              <button class="lightbox-btn" @click="lightboxZoomOut" title="缩小"><span>−</span></button>
              <span class="lightbox-zoom">{{ Math.round(lightboxScale * 100) }}%</span>
              <button class="lightbox-btn" @click="lightboxZoomIn" title="放大"><span>+</span></button>
              <button class="lightbox-btn" @click="lightboxFit" title="适应窗口"><span>⊡</span></button>
              <button class="lightbox-btn" @click="lightboxActual" title="实际大小"><span>1:1</span></button>
              <a :href="imageUrl" download class="lightbox-btn" title="下载"><span>↓</span></a>
              <button class="lightbox-btn close-btn" @click="closeLightbox" title="关闭 (Esc)"><span>✕</span></button>
            </div>
          </div>
          <div class="lightbox-content" ref="lightboxRef" @wheel.prevent="handleLightboxWheel"
            @mousedown="startLightboxDrag" @mousemove="onLightboxDrag" @mouseup="endLightboxDrag" @mouseleave="endLightboxDrag"
            :class="{ dragging: isLightboxDragging }">
            <button v-if="siblingImages.length > 1 && hasPrev" class="lightbox-nav lightbox-nav-left" @click="prevImage">‹</button>
            <div class="lightbox-image-wrapper" :style="lightboxWrapperStyle">
              <img :src="imageUrl" class="lightbox-image" draggable="false" @load="handleLightboxLoad" />
            </div>
            <button v-if="siblingImages.length > 1 && hasNext" class="lightbox-nav lightbox-nav-right" @click="nextImage">›</button>
          </div>
          <div class="lightbox-footer" v-if="siblingImages.length > 1">
            <span>{{ currentIndex + 1 }} / {{ siblingImages.length }}</span>
            <span class="lightbox-hint">← → 切换图片 · Esc 关闭 · 滚轮缩放</span>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'

const props = defineProps<{ path: string; siblingImages: string[] }>()
const emit = defineEmits<{ (e: 'navigate', path: string): void }>()

const containerRef = ref<HTMLElement | null>(null)
const imageRef = ref<HTMLImageElement | null>(null)
const lightboxRef = ref<HTMLElement | null>(null)

const loading = ref(true)
const imageLoaded = ref(false)
const error = ref<string | null>(null)
const scale = ref(1)
const translateX = ref(0)
const translateY = ref(0)
const imageInfo = ref<{ width: number; height: number } | null>(null)

const isDragging = ref(false)
const hasDragged = ref(false)
const dragStartX = ref(0)
const dragStartY = ref(0)
const dragStartTranslateX = ref(0)
const dragStartTranslateY = ref(0)

// 灯箱状态
const lightboxOpen = ref(false)
const lightboxScale = ref(1)
const lightboxTranslateX = ref(0)
const lightboxTranslateY = ref(0)
const isLightboxDragging = ref(false)
const lightboxDragStartX = ref(0)
const lightboxDragStartY = ref(0)
const lightboxDragStartTranslateX = ref(0)
const lightboxDragStartTranslateY = ref(0)

const currentIndex = computed(() => props.siblingImages.indexOf(props.path))
const hasPrev = computed(() => currentIndex.value > 0)
const hasNext = computed(() => currentIndex.value < props.siblingImages.length - 1)
const imageUrl = computed(() => `/api/files/download?path=${encodeURIComponent(props.path)}`)
const fileName = computed(() => { const parts = props.path.split('/'); return parts[parts.length - 1] || props.path })
const wrapperStyle = computed(() => ({ transform: `translate(${translateX.value}px, ${translateY.value}px) scale(${scale.value})` }))
const lightboxWrapperStyle = computed(() => ({ transform: `translate(${lightboxTranslateX.value}px, ${lightboxTranslateY.value}px) scale(${lightboxScale.value})` }))

watch(() => props.path, () => { loading.value = true; imageLoaded.value = false; error.value = null; resetView(); if (lightboxOpen.value) resetLightbox() })

function prevImage() { if (hasPrev.value) emit('navigate', props.siblingImages[currentIndex.value - 1]) }
function nextImage() { if (hasNext.value) emit('navigate', props.siblingImages[currentIndex.value + 1]) }

function handleImageLoad(e: Event) {
  loading.value = false; imageLoaded.value = true
  const img = e.target as HTMLImageElement
  imageInfo.value = { width: img.naturalWidth, height: img.naturalHeight }
  fitToWindow()
}
function handleImageError() { loading.value = false; error.value = '无法加载图片' }

function zoomIn() { if (scale.value < 10) scale.value = Math.min(10, scale.value * 1.25) }
function zoomOut() { if (scale.value > 0.1) scale.value = Math.max(0.1, scale.value / 1.25) }
function resetView() { scale.value = 1; translateX.value = 0; translateY.value = 0 }
function actualSize() { scale.value = 1; translateX.value = 0; translateY.value = 0 }
function fitToWindow() {
  if (!containerRef.value || !imageInfo.value) return
  const cw = containerRef.value.clientWidth - 120, ch = containerRef.value.clientHeight - 60
  scale.value = Math.min(cw / imageInfo.value.width, ch / imageInfo.value.height, 1)
  translateX.value = 0; translateY.value = 0
}
function handleWheel(e: WheelEvent) { scale.value = Math.max(0.1, Math.min(10, scale.value * (e.deltaY > 0 ? 0.9 : 1.1))) }

function startDrag(e: MouseEvent) {
  if (!imageLoaded.value || (e.target as HTMLElement).closest('.nav-arrow')) return
  isDragging.value = true; hasDragged.value = false
  dragStartX.value = e.clientX; dragStartY.value = e.clientY
  dragStartTranslateX.value = translateX.value; dragStartTranslateY.value = translateY.value
}
function onDrag(e: MouseEvent) {
  if (!isDragging.value) return
  const dx = e.clientX - dragStartX.value, dy = e.clientY - dragStartY.value
  if (Math.abs(dx) > 5 || Math.abs(dy) > 5) hasDragged.value = true
  translateX.value = dragStartTranslateX.value + dx; translateY.value = dragStartTranslateY.value + dy
}
function endDrag() { isDragging.value = false }

// 灯箱功能
function handleImageClick() { if (!hasDragged.value) openLightbox() }
function openLightbox() { lightboxOpen.value = true; document.body.style.overflow = 'hidden'; nextTick(() => resetLightbox()) }
function closeLightbox() { lightboxOpen.value = false; document.body.style.overflow = '' }
function resetLightbox() { lightboxScale.value = 1; lightboxTranslateX.value = 0; lightboxTranslateY.value = 0 }
function handleLightboxLoad() { lightboxFit() }
function lightboxZoomIn() { if (lightboxScale.value < 10) lightboxScale.value = Math.min(10, lightboxScale.value * 1.25) }
function lightboxZoomOut() { if (lightboxScale.value > 0.1) lightboxScale.value = Math.max(0.1, lightboxScale.value / 1.25) }
function lightboxFit() {
  if (!lightboxRef.value || !imageInfo.value) return
  const cw = lightboxRef.value.clientWidth - 100, ch = lightboxRef.value.clientHeight - 100
  lightboxScale.value = Math.min(cw / imageInfo.value.width, ch / imageInfo.value.height, 1)
  lightboxTranslateX.value = 0; lightboxTranslateY.value = 0
}
function lightboxActual() { lightboxScale.value = 1; lightboxTranslateX.value = 0; lightboxTranslateY.value = 0 }
function handleLightboxWheel(e: WheelEvent) { lightboxScale.value = Math.max(0.1, Math.min(10, lightboxScale.value * (e.deltaY > 0 ? 0.9 : 1.1))) }

function startLightboxDrag(e: MouseEvent) {
  if ((e.target as HTMLElement).closest('.lightbox-nav')) return
  isLightboxDragging.value = true; lightboxDragStartX.value = e.clientX; lightboxDragStartY.value = e.clientY
  lightboxDragStartTranslateX.value = lightboxTranslateX.value; lightboxDragStartTranslateY.value = lightboxTranslateY.value
}
function onLightboxDrag(e: MouseEvent) { if (!isLightboxDragging.value) return; lightboxTranslateX.value = lightboxDragStartTranslateX.value + e.clientX - lightboxDragStartX.value; lightboxTranslateY.value = lightboxDragStartTranslateY.value + e.clientY - lightboxDragStartY.value }
function endLightboxDrag() { isLightboxDragging.value = false }

function handleKeydown(e: KeyboardEvent) {
  if ((e.target as HTMLElement).tagName === 'INPUT' || (e.target as HTMLElement).tagName === 'TEXTAREA') return
  if (e.key === '+' || e.key === '=') { lightboxOpen.value ? lightboxZoomIn() : zoomIn() }
  else if (e.key === '-') { lightboxOpen.value ? lightboxZoomOut() : zoomOut() }
  else if (e.key === '0') { lightboxOpen.value ? resetLightbox() : resetView() }
  else if (e.key === 'ArrowLeft') { e.preventDefault(); prevImage() }
  else if (e.key === 'ArrowRight') { e.preventDefault(); nextImage() }
  else if (e.key === ' ' && !lightboxOpen.value) { e.preventDefault(); openLightbox() }
  else if (e.key === 'Escape' && lightboxOpen.value) { closeLightbox() }
}

onMounted(() => { loading.value = true; imageLoaded.value = false; error.value = null; window.addEventListener('keydown', handleKeydown) })
onUnmounted(() => { window.removeEventListener('keydown', handleKeydown); document.body.style.overflow = '' })
</script>

<style scoped>
.image-preview { height: 100%; display: flex; flex-direction: column; background: var(--bg-secondary); }
.image-toolbar { display: flex; align-items: center; justify-content: center; padding: 8px 16px; background: var(--bg-primary); border-bottom: 1px solid var(--border-color); gap: 12px; flex-wrap: wrap; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
.toolbar-group { display: flex; align-items: center; gap: 4px; }
.nav-group { gap: 8px; }
.toolbar-divider { width: 1px; height: 20px; background: var(--border-color); margin: 0 4px; }
.toolbar-btn { width: 32px; height: 32px; border: 1px solid var(--border-color); background: var(--bg-primary); border-radius: 6px; cursor: pointer; font-size: 14px; color: var(--text-secondary); transition: all 0.15s ease; display: flex; align-items: center; justify-content: center; }
.toolbar-btn:hover:not(:disabled) { background: var(--bg-tertiary); color: var(--text-primary); border-color: var(--primary-color); }
.toolbar-btn:active:not(:disabled) { transform: scale(0.95); }
.toolbar-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.toolbar-btn .icon { font-weight: 600; font-size: 14px; }
.nav-btn .icon { font-size: 20px; font-weight: 300; }
.nav-info { font-size: 13px; color: var(--text-secondary); min-width: 50px; text-align: center; font-family: monospace; }
.link-btn { text-decoration: none; }
.zoom-info { min-width: 52px; text-align: center; font-size: 13px; font-weight: 500; color: var(--text-secondary); font-family: monospace; }
.image-container { flex: 1; overflow: hidden; display: flex; align-items: center; justify-content: center; position: relative; background: radial-gradient(circle at 50% 50%, #2a2a3a 0%, #1a1a2e 100%); }
.image-container::before { content: ''; position: absolute; inset: 0; background-image: linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px); background-size: 20px 20px; pointer-events: none; }
.image-container.grabbable { cursor: grab; }
.image-container.dragging { cursor: grabbing; }
.nav-arrow { position: absolute; top: 50%; transform: translateY(-50%); width: 48px; height: 80px; background: rgba(0,0,0,0.4); border: none; color: white; font-size: 36px; cursor: pointer; z-index: 10; display: flex; align-items: center; justify-content: center; transition: all 0.2s ease; opacity: 0.6; }
.nav-arrow:hover { background: rgba(0,0,0,0.6); opacity: 1; }
.nav-arrow-left { left: 0; border-radius: 0 8px 8px 0; }
.nav-arrow-right { right: 0; border-radius: 8px 0 0 8px; }
.image-loading, .image-error { display: flex; flex-direction: column; align-items: center; justify-content: center; color: rgba(255,255,255,0.7); background: rgba(0,0,0,0.3); padding: 32px 48px; border-radius: 12px; backdrop-filter: blur(8px); z-index: 1; }
.loading-spinner { width: 40px; height: 40px; border: 3px solid rgba(255,255,255,0.2); border-top-color: var(--primary-color); border-radius: 50%; animation: spin 0.8s linear infinite; margin-bottom: 12px; }
@keyframes spin { to { transform: rotate(360deg); } }
.image-error { color: #ff6b6b; }
.error-icon { font-size: 48px; margin-bottom: 12px; opacity: 0.8; }
.error-title { font-size: 16px; font-weight: 500; margin-bottom: 8px; }
.error-message { font-size: 13px; color: rgba(255,255,255,0.5); }
.image-wrapper { position: relative; transform-origin: center center; transition: transform 0.1s ease-out; z-index: 1; cursor: zoom-in; }
.preview-image { display: block; max-width: none; max-height: none; box-shadow: 0 4px 20px rgba(0,0,0,0.4), 0 0 0 1px rgba(255,255,255,0.1); border-radius: 4px; user-select: none; }
.image-info { display: flex; align-items: center; justify-content: center; gap: 16px; padding: 8px 16px; background: var(--bg-primary); border-top: 1px solid var(--border-color); font-size: 12px; color: var(--text-muted); }
.info-item { display: flex; align-items: center; gap: 4px; }
.info-label { color: var(--text-muted); opacity: 0.7; }
.info-divider { color: var(--border-color); }
.hint { color: var(--primary-color); opacity: 0.8; }
</style>

<style>
/* 灯箱样式 - 全局 */
.lightbox-overlay { position: fixed; inset: 0; z-index: 9999; background: rgba(0,0,0,0.95); display: flex; flex-direction: column; }
.lightbox-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 20px; background: rgba(255,255,255,0.05); border-bottom: 1px solid rgba(255,255,255,0.1); }
.lightbox-title { color: white; font-size: 14px; font-weight: 500; opacity: 0.9; }
.lightbox-actions { display: flex; align-items: center; gap: 8px; }
.lightbox-btn { width: 36px; height: 36px; border: 1px solid rgba(255,255,255,0.2); background: rgba(255,255,255,0.1); border-radius: 8px; cursor: pointer; font-size: 16px; color: white; display: flex; align-items: center; justify-content: center; transition: all 0.15s ease; text-decoration: none; }
.lightbox-btn:hover { background: rgba(255,255,255,0.2); border-color: rgba(255,255,255,0.3); }
.lightbox-btn.close-btn { background: rgba(239,68,68,0.3); border-color: rgba(239,68,68,0.5); }
.lightbox-btn.close-btn:hover { background: rgba(239,68,68,0.5); }
.lightbox-zoom { color: white; font-size: 13px; min-width: 50px; text-align: center; font-family: monospace; opacity: 0.8; }
.lightbox-content { flex: 1; display: flex; align-items: center; justify-content: center; position: relative; overflow: hidden; cursor: grab; }
.lightbox-content.dragging { cursor: grabbing; }
.lightbox-nav { position: absolute; top: 50%; transform: translateY(-50%); width: 60px; height: 100px; background: rgba(255,255,255,0.1); border: none; color: white; font-size: 48px; cursor: pointer; z-index: 10; display: flex; align-items: center; justify-content: center; transition: all 0.2s ease; opacity: 0.5; }
.lightbox-nav:hover { background: rgba(255,255,255,0.2); opacity: 1; }
.lightbox-nav-left { left: 20px; border-radius: 12px; }
.lightbox-nav-right { right: 20px; border-radius: 12px; }
.lightbox-image-wrapper { transform-origin: center center; transition: transform 0.1s ease-out; }
.lightbox-image { max-width: none; max-height: none; box-shadow: 0 8px 40px rgba(0,0,0,0.5); border-radius: 8px; user-select: none; }
.lightbox-footer { display: flex; align-items: center; justify-content: center; gap: 24px; padding: 12px 20px; background: rgba(255,255,255,0.05); border-top: 1px solid rgba(255,255,255,0.1); color: white; font-size: 13px; }
.lightbox-hint { opacity: 0.5; font-size: 12px; }
/* 动画 */
.lightbox-enter-active, .lightbox-leave-active { transition: all 0.3s ease; }
.lightbox-enter-from, .lightbox-leave-to { opacity: 0; }
.lightbox-enter-from .lightbox-image-wrapper, .lightbox-leave-to .lightbox-image-wrapper { transform: scale(0.9); }
</style>
