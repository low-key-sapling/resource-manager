<template>
  <div 
    class="file-card"
    :class="{ selected, directory: node.type === 'directory', 'drag-over': isDragOver, 'has-thumbnail': isImage }"
    draggable="true"
    @click="$emit('click', node)"
    @dblclick="$emit('dblclick', node)"
    @contextmenu.prevent="$emit('contextmenu', $event, node)"
    @dragstart="handleDragStart"
    @dragend="handleDragEnd"
    @dragover.prevent="handleDragOver"
    @dragleave="handleDragLeave"
    @drop.prevent="handleDrop"
  >
    <!-- 图片缩略图 -->
    <div v-if="isImage" class="file-thumbnail">
      <img 
        :src="thumbnailUrl" 
        :alt="node.name"
        class="thumbnail-image"
        @error="handleThumbnailError"
        @load="thumbnailLoaded = true"
      />
      <div v-if="!thumbnailLoaded" class="thumbnail-placeholder">
        <span class="thumbnail-icon">{{ icon }}</span>
      </div>
    </div>
    <!-- 普通文件图标 -->
    <div v-else class="file-icon">
      {{ icon }}
    </div>
    <div class="file-info">
      <div class="file-name" :title="node.name">
        {{ node.name }}
      </div>
      <div class="file-meta">
        <span v-if="node.type === 'file' && node.size !== undefined" class="file-size">
          {{ formatSize(node.size) }}
        </span>
        <span v-else-if="node.type === 'directory'" class="file-type">
          文件夹
        </span>
        <span v-if="node.lastModified" class="file-date">
          {{ formatDate(node.lastModified) }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { FileNode } from '@/types'
import { getFileIcon } from '@/utils/fileIcons'

const props = defineProps<{
  node: FileNode
  selected?: boolean
}>()

const emit = defineEmits<{
  (e: 'click', node: FileNode): void
  (e: 'dblclick', node: FileNode): void
  (e: 'contextmenu', event: MouseEvent, node: FileNode): void
  (e: 'drop', data: { source: string, target: string }): void
}>()

const isDragOver = ref(false)
const thumbnailLoaded = ref(false)

const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp', 'svg', 'ico']
const isImage = computed(() => {
  return props.node.type === 'file' && 
    props.node.extension && 
    imageExtensions.includes(props.node.extension.toLowerCase())
})

const thumbnailUrl = computed(() => {
  if (!isImage.value) return ''
  return `/api/files/download?path=${encodeURIComponent(props.node.path)}`
})

const icon = computed(() => getFileIcon(props.node.type, props.node.extension))

function handleThumbnailError() {
  thumbnailLoaded.value = false
}

function handleDragStart(e: DragEvent) {
  if (e.dataTransfer) {
    e.dataTransfer.setData('text/plain', props.node.path)
    e.dataTransfer.effectAllowed = 'move'
  }
}

function handleDragEnd() {
  isDragOver.value = false
}

function handleDragOver(e: DragEvent) {
  if (props.node.type === 'directory') {
    isDragOver.value = true
    if (e.dataTransfer) {
      e.dataTransfer.dropEffect = 'move'
    }
  }
}

function handleDragLeave() {
  isDragOver.value = false
}

function handleDrop(e: DragEvent) {
  isDragOver.value = false
  if (props.node.type !== 'directory') return
  
  const sourcePath = e.dataTransfer?.getData('text/plain')
  if (sourcePath && sourcePath !== props.node.path) {
    emit('drop', { source: sourcePath, target: props.node.path })
  }
}

function formatSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

function formatDate(dateStr: string): string {
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('zh-CN', {
      month: 'short',
      day: 'numeric'
    })
  } catch {
    return ''
  }
}
</script>

<style scoped>
.file-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-md);
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  min-width: 120px;
  max-width: 160px;
}

.file-card:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.file-card.selected {
  border-color: var(--primary-color);
  background: var(--primary-light);
}

.file-card.directory {
  background: var(--bg-secondary);
}

.file-card.directory:hover {
  background: var(--bg-primary);
}

.file-card.drag-over {
  border-color: var(--primary-color);
  background: var(--primary-light);
  transform: scale(1.02);
}

.file-icon {
  font-size: 48px;
  margin-bottom: var(--spacing-sm);
  line-height: 1;
}

/* 图片缩略图 */
.file-thumbnail {
  width: 100%;
  height: 80px;
  margin-bottom: var(--spacing-sm);
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--bg-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.thumbnail-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-fast);
}

.file-card:hover .thumbnail-image {
  transform: scale(1.05);
}

.thumbnail-placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-tertiary);
}

.thumbnail-icon {
  font-size: 36px;
  opacity: 0.5;
}

.file-card.has-thumbnail {
  padding-top: var(--spacing-sm);
}

.file-info {
  width: 100%;
  text-align: center;
}

.file-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: var(--spacing-xs);
}

.file-meta {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  font-size: 11px;
  color: var(--text-muted);
}

.file-size,
.file-type,
.file-date {
  display: block;
}
</style>
