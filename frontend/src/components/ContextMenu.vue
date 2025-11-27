<template>
  <Teleport to="body">
    <div 
      v-if="visible" 
      class="context-menu-overlay"
      @click="close"
      @contextmenu.prevent="close"
    >
      <div 
        class="context-menu"
        :style="menuStyle"
        @click.stop
      >
        <template v-for="item in items" :key="item.id">
          <div v-if="item.divider" class="menu-divider"></div>
          <button
            v-else
            class="menu-item"
            :class="{ disabled: item.disabled }"
            :disabled="item.disabled"
            @click="handleSelect(item.id)"
          >
            <span v-if="item.icon" class="menu-icon">{{ item.icon }}</span>
            <span class="menu-label">{{ item.label }}</span>
            <span v-if="item.shortcut" class="menu-shortcut">{{ item.shortcut }}</span>
          </button>
        </template>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue'

export interface MenuItem {
  id: string
  label: string
  icon?: string
  shortcut?: string
  disabled?: boolean
  divider?: boolean
}

const props = defineProps<{
  items: MenuItem[]
  x: number
  y: number
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'select', id: string): void
  (e: 'close'): void
}>()

const menuStyle = computed(() => {
  // 确保菜单不超出视口
  const menuWidth = 200
  const menuHeight = props.items.length * 36
  
  let x = props.x
  let y = props.y
  
  if (x + menuWidth > window.innerWidth) {
    x = window.innerWidth - menuWidth - 10
  }
  
  if (y + menuHeight > window.innerHeight) {
    y = window.innerHeight - menuHeight - 10
  }
  
  return {
    left: `${x}px`,
    top: `${y}px`
  }
})

function handleSelect(id: string) {
  emit('select', id)
  close()
}

function close() {
  emit('close')
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') {
    close()
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.context-menu-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
}

.context-menu {
  position: fixed;
  min-width: 180px;
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  padding: var(--spacing-xs);
  z-index: 1001;
  animation: scaleIn var(--transition-fast);
}

.menu-item {
  display: flex;
  align-items: center;
  width: 100%;
  padding: var(--spacing-sm) var(--spacing-md);
  border: none;
  background: transparent;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: 14px;
  color: var(--text-primary);
  text-align: left;
  transition: background var(--transition-fast);
}

.menu-item:hover:not(:disabled) {
  background: var(--bg-hover);
}

.menu-item.disabled,
.menu-item:disabled {
  color: var(--text-muted);
  cursor: not-allowed;
}

.menu-icon {
  width: 20px;
  margin-right: var(--spacing-sm);
  text-align: center;
}

.menu-label {
  flex: 1;
}

.menu-shortcut {
  font-size: 12px;
  color: var(--text-muted);
  margin-left: var(--spacing-md);
}

.menu-divider {
  height: 1px;
  background: var(--border-color);
  margin: var(--spacing-xs) 0;
}
</style>
