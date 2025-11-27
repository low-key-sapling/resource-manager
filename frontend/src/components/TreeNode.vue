<template>
  <div class="tree-node">
    <div 
      class="node-content"
      :class="{ selected: isSelected }"
      :style="{ paddingLeft: `${depth * 16}px` }"
      @click="handleClick"
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
      <span class="node-name">{{ node.name }}</span>
    </div>
    
    <div v-if="node.type === 'directory' && isExpanded && node.children" class="children">
      <TreeNode
        v-for="child in sortedChildren"
        :key="child.path"
        :node="child"
        :depth="depth + 1"
        :selected-path="selectedPath"
        @select="$emit('select', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { FileNode } from '@/types'
import { getFileIcon } from '@/utils/fileIcons'
import { sortFileNodes } from '@/utils/fileSort'

const props = defineProps<{
  node: FileNode
  depth?: number
  selectedPath?: string
}>()

const emit = defineEmits<{
  select: [node: FileNode]
}>()

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
  emit('select', props.node)
}
</script>

<style scoped>
.tree-node {
  user-select: none;
}

.node-content {
  display: flex;
  align-items: center;
  padding: 4px 8px;
  cursor: pointer;
  border-radius: 4px;
}

.node-content:hover {
  background-color: #f0f0f0;
}

.node-content.selected {
  background-color: #e3f2fd;
}

.expand-icon {
  width: 16px;
  font-size: 10px;
  color: #666;
  cursor: pointer;
}

.expand-icon-placeholder {
  width: 16px;
}

.node-icon {
  margin-right: 6px;
  font-size: 14px;
}

.node-name {
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.children {
  margin-left: 0;
}
</style>
