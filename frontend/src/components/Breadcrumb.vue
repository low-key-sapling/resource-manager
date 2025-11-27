<template>
  <nav class="breadcrumb" aria-label="面包屑导航">
    <ol class="breadcrumb-list">
      <li class="breadcrumb-item">
        <button 
          class="breadcrumb-link" 
          @click="$emit('navigate', '/')"
          :class="{ active: path === '/' }"
        >
          <span class="breadcrumb-icon">🏠</span>
          <span class="breadcrumb-text">根目录</span>
        </button>
      </li>
      
      <template v-for="(item, index) in pathItems" :key="item.path">
        <li class="breadcrumb-separator" aria-hidden="true">
          <span>›</span>
        </li>
        <li class="breadcrumb-item">
          <button 
            class="breadcrumb-link"
            :class="{ active: index === pathItems.length - 1 }"
            @click="$emit('navigate', item.path)"
            :disabled="index === pathItems.length - 1"
          >
            <span class="breadcrumb-text">{{ item.name }}</span>
          </button>
        </li>
      </template>
    </ol>
  </nav>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface PathItem {
  name: string
  path: string
}

const props = defineProps<{
  path: string
}>()

defineEmits<{
  (e: 'navigate', path: string): void
}>()

const pathItems = computed<PathItem[]>(() => {
  if (!props.path || props.path === '/') return []
  
  const parts = props.path.split('/').filter(Boolean)
  const items: PathItem[] = []
  let currentPath = ''
  
  for (const part of parts) {
    currentPath += '/' + part
    items.push({
      name: part,
      path: currentPath
    })
  }
  
  return items
})
</script>

<style scoped>
.breadcrumb {
  flex: 1;
  min-width: 0;
}

.breadcrumb-list {
  display: flex;
  align-items: center;
  list-style: none;
  margin: 0;
  padding: 0;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
}

.breadcrumb-item {
  display: flex;
  align-items: center;
  min-width: 0;
}

.breadcrumb-separator {
  color: var(--text-muted);
  font-size: 14px;
  user-select: none;
}

.breadcrumb-link {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-xs) var(--spacing-sm);
  border: none;
  background: transparent;
  color: var(--text-secondary);
  font-size: 14px;
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
  max-width: 200px;
}

.breadcrumb-link:hover:not(:disabled) {
  background: var(--bg-tertiary);
  color: var(--primary-color);
}

.breadcrumb-link.active {
  color: var(--text-primary);
  font-weight: 500;
  cursor: default;
}

.breadcrumb-link:disabled {
  cursor: default;
}

.breadcrumb-icon {
  font-size: 14px;
  flex-shrink: 0;
}

.breadcrumb-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
