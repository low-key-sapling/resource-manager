<template>
  <div 
    class="file-card"
    :class="{ selected, directory: node.type === 'directory', 'drag-over': isDragOver }"
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
    <div class="file-icon">
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

const icon = computed(() => getFileIcon(props.node.type, props.node.extension))

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
