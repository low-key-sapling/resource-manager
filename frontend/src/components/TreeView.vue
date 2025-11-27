<template>
  <div class="tree-view">
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="tree" class="tree-container">
      <TreeNode
        :node="tree"
        :selected-path="selectedPath"
        @select="handleSelect"
      />
    </div>
    <div v-else class="empty">暂无文件</div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { FileNode } from '@/types'
import { fileApi } from '@/services/api'
import TreeNode from './TreeNode.vue'

const props = defineProps<{
  selectedPath?: string
}>()

const emit = defineEmits<{
  select: [node: FileNode]
}>()

const tree = ref<FileNode | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

async function loadTree() {
  loading.value = true
  error.value = null
  try {
    tree.value = await fileApi.getTree('/')
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

function handleSelect(node: FileNode) {
  emit('select', node)
}

async function refresh() {
  await loadTree()
}

function findNodeByPath(node: FileNode, path: string): FileNode | null {
  if (node.path === path) return node
  if (node.children) {
    for (const child of node.children) {
      const found = findNodeByPath(child, path)
      if (found) return found
    }
  }
  return null
}

function navigateToPath(path: string) {
  if (!tree.value) return
  const node = findNodeByPath(tree.value, path)
  if (node) {
    emit('select', node)
  }
}

onMounted(() => {
  loadTree()
})

defineExpose({ refresh, navigateToPath })
</script>

<style scoped>
.tree-view {
  height: 100%;
  overflow: auto;
  padding: var(--spacing-sm);
}

.loading, .error, .empty {
  padding: var(--spacing-lg);
  text-align: center;
  color: var(--text-secondary);
}

.error {
  color: var(--error-color);
}

.tree-container {
  min-width: max-content;
}
</style>
