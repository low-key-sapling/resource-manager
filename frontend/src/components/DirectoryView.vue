<template>
  <div class="directory-view">
    <div class="directory-header">
      <div class="directory-info">
        <span class="item-count">{{ files.length }} 个项目</span>
      </div>
      <div class="view-options">
        <button 
          class="icon-btn" 
          :class="{ active: viewMode === 'grid' }"
          @click="viewMode = 'grid'"
          title="网格视图"
        >
          ⊞
        </button>
        <button 
          class="icon-btn" 
          :class="{ active: viewMode === 'list' }"
          @click="viewMode = 'list'"
          title="列表视图"
        >
          ☰
        </button>
      </div>
    </div>
    
    <div class="directory-content" :class="viewMode">
      <div v-if="sortedFiles.length === 0" class="empty-directory">
        <div class="empty-icon">📂</div>
        <p>此目录为空</p>
      </div>
      
      <template v-else>
        <!-- 网格视图 -->
        <template v-if="viewMode === 'grid'">
          <FileCard
            v-for="file in sortedFiles"
            :key="file.path"
            :node="file"
            :selected="selectedPath === file.path"
            @click="handleClick"
            @dblclick="handleDoubleClick"
            @contextmenu="handleContextMenu"
          />
        </template>
        
        <!-- 列表视图 -->
        <template v-else>
          <div 
            v-for="file in sortedFiles"
            :key="file.path"
            class="list-item"
            :class="{ selected: selectedPath === file.path }"
            @click="handleClick(file)"
            @dblclick="handleDoubleClick(file)"
            @contextmenu.prevent="handleContextMenu($event, file)"
          >
            <span class="list-icon">{{ getIcon(file) }}</span>
            <span class="list-name">{{ file.name }}</span>
            <span class="list-size">{{ file.type === 'file' ? formatSize(file.size) : '--' }}</span>
            <span class="list-date">{{ formatDate(file.lastModified) }}</span>
          </div>
        </template>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { FileNode } from '@/types'
import FileCard from './FileCard.vue'
import { getFileIcon } from '@/utils/fileIcons'
import { sortFileNodes } from '@/utils/fileSort'

const props = defineProps<{
  path: string
  files: FileNode[]
}>()

const emit = defineEmits<{
  (e: 'select', node: FileNode): void
  (e: 'navigate', path: string): void
  (e: 'contextmenu', event: MouseEvent, node: FileNode): void
}>()

const viewMode = ref<'grid' | 'list'>('grid')
const selectedPath = ref<string>('')

const sortedFiles = computed(() => sortFileNodes(props.files))

function getIcon(node: FileNode): string {
  return getFileIcon(node.type, node.extension)
}

function formatSize(bytes?: number): string {
  if (bytes === undefined || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return '--'
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('zh-CN')
  } catch {
    return '--'
  }
}

function handleClick(node: FileNode) {
  selectedPath.value = node.path
  emit('select', node)
}

function handleDoubleClick(node: FileNode) {
  if (node.type === 'directory') {
    emit('navigate', node.path)
  } else {
    emit('select', node)
  }
}

function handleContextMenu(event: MouseEvent, node: FileNode) {
  emit('contextmenu', event, node)
}
</script>

<style scoped>
.directory-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--bg-secondary);
}

.directory-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
}

.directory-info {
  font-size: 13px;
  color: var(--text-secondary);
}

.view-options {
  display: flex;
  gap: var(--spacing-xs);
}

.icon-btn {
  background: transparent;
  border: 1px solid transparent;
  color: var(--text-secondary);
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  transition: all var(--transition-fast);
}

.icon-btn:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.icon-btn.active {
  background: var(--primary-light);
  color: var(--primary-color);
  border-color: var(--primary-color);
}

.directory-content {
  flex: 1;
  overflow: auto;
  padding: var(--spacing-md);
}

.directory-content.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: var(--spacing-md);
  align-content: start;
}

.directory-content.list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.empty-directory {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-xl);
  color: var(--text-muted);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: var(--spacing-sm);
  opacity: 0.5;
}

/* 列表视图样式 */
.list-item {
  display: grid;
  grid-template-columns: 32px 1fr 100px 120px;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--bg-primary);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.list-item:hover {
  background: var(--bg-hover);
}

.list-item.selected {
  background: var(--primary-light);
}

.list-icon {
  font-size: 20px;
  text-align: center;
}

.list-name {
  font-size: 14px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.list-size,
.list-date {
  font-size: 12px;
  color: var(--text-muted);
  text-align: right;
}

@media (max-width: 768px) {
  .directory-content.grid {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  }
  
  .list-item {
    grid-template-columns: 32px 1fr;
  }
  
  .list-size,
  .list-date {
    display: none;
  }
}
</style>
