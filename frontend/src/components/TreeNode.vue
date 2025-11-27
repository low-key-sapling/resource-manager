<template>
  <div class="tree-node">
    <div 
      class="node-content"
      :class="{ selected: isSelected, editing: isEditing }"
      :style="{ paddingLeft: `${depth * 16}px` }"
      @click="handleClick"
      @contextmenu.prevent="handleContextMenu"
    >
      <span 
        v-if="node.type === 'directory'" 
        class="expand-icon"
        @click.stop="toggleExpand"
      >
        {{ isExpanded ? '▼' : '▶' }}
      </span>
      <span v-else class="expand-icon-placeholder"></span>
      <span class="node-icon">{{ icon }}</span>
      
      <input
        v-if="isEditing"
        ref="editInputRef"
        v-model="editName"
        class="edit-input"
        @blur="finishEdit"
        @keyup.enter="finishEdit"
        @keyup.escape="cancelEdit"
        @click.stop
      />
      <span v-else class="node-name">{{ node.name }}</span>
    </div>
    
    <div v-if="node.type === 'directory' && isExpanded && node.children" class="children">
      <TreeNode
        v-for="child in sortedChildren"
        :key="child.path"
        :node="child"
        :depth="depth + 1"
        :selected-path="selectedPath"
        @select="$emit('select', $event)"
        @contextmenu="$emit('contextmenu', $event)"
        @rename="$emit('rename', $event)"
        @delete="$emit('delete', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, nextTick } from 'vue'
import type { FileNode } from '@/types'
import { getFileIcon } from '@/utils/fileIcons'
import { sortFileNodes } from '@/utils/fileSort'

const props = defineProps<{
  node: FileNode
  depth?: number
  selectedPath?: string
}>()

const emit = defineEmits<{
  (e: 'select', node: FileNode): void
  (e: 'contextmenu', event: { mouseEvent: MouseEvent, node: FileNode }): void
  (e: 'rename', data: { node: FileNode, newName: string }): void
  (e: 'delete', node: FileNode): void
}>()

const editInputRef = ref<HTMLInputElement | null>(null)
const isEditing = ref(false)
const editName = ref('')

const depth = computed(() => props.depth ?? 0)
const isExpanded = ref(props.depth === 0)
const isSelected = computed(() => props.selectedPath === props.node.path)

const icon = computed(() => getFileIcon(props.node.type, props.node.extension))

const sortedChildren = computed(() => {
  if (props.node.children) {
    return sortFileNodes(props.node.children)
  }
  return []
})

function toggleExpand() {
  isExpanded.value = !isExpanded.value
}

function handleClick() {
  if (!isEditing.value) {
    emit('select', props.node)
  }
}

function handleContextMenu(event: MouseEvent) {
  emit('contextmenu', { mouseEvent: event, node: props.node })
}

function startEdit() {
  isEditing.value = true
  editName.value = props.node.name
  nextTick(() => {
    editInputRef.value?.focus()
    editInputRef.value?.select()
  })
}

function finishEdit() {
  if (isEditing.value && editName.value && editName.value !== props.node.name) {
    emit('rename', { node: props.node, newName: editName.value })
  }
  isEditing.value = false
}

function cancelEdit() {
  isEditing.value = false
  editName.value = props.node.name
}

defineExpose({ startEdit })
</script>

<style scoped>
.tree-node {
  user-select: none;
}

.node-content {
  display: flex;
  align-items: center;
  padding: var(--spacing-xs) var(--spacing-sm);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
}

.node-content:hover {
  background-color: var(--bg-tertiary);
}

.node-content.selected {
  background-color: var(--primary-light);
}

.node-content.editing {
  background-color: var(--bg-primary);
}

.expand-icon {
  width: 16px;
  font-size: 10px;
  color: var(--text-muted);
  cursor: pointer;
  flex-shrink: 0;
}

.expand-icon:hover {
  color: var(--text-primary);
}

.expand-icon-placeholder {
  width: 16px;
  flex-shrink: 0;
}

.node-icon {
  margin-right: var(--spacing-xs);
  font-size: 14px;
  flex-shrink: 0;
}

.node-name {
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-primary);
}

.edit-input {
  flex: 1;
  padding: 2px var(--spacing-xs);
  border: 1px solid var(--primary-color);
  border-radius: var(--radius-sm);
  font-size: 13px;
  outline: none;
  background: var(--bg-primary);
}

.children {
  margin-left: 0;
}
</style>
