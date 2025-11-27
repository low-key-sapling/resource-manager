<template>
  <header class="app-header">
    <div class="header-left">
      <button 
        class="menu-toggle" 
        @click="$emit('toggle-menu')"
        :title="sidebarCollapsed ? '展开侧边栏' : '收起侧边栏'"
      >
        <span class="menu-icon">{{ sidebarCollapsed ? '☰' : '✕' }}</span>
      </button>
      <div class="brand">
        <span class="brand-icon">📁</span>
        <h1 class="brand-title">{{ title }}</h1>
      </div>
    </div>
    
    <div class="header-right">
      <slot name="actions"></slot>
    </div>
  </header>
</template>

<script setup lang="ts">
defineProps<{
  title?: string
  sidebarCollapsed?: boolean
}>()

defineEmits<{
  (e: 'toggle-menu'): void
}>()
</script>

<style scoped>
.app-header {
  background: var(--primary-gradient);
  color: white;
  padding: var(--spacing-md) var(--spacing-lg);
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: var(--shadow-md);
  z-index: 20;
  min-height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.menu-toggle {
  display: none;
  background: rgba(255, 255, 255, 0.15);
  border: none;
  color: white;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: 18px;
  transition: background var(--transition-fast);
  align-items: center;
  justify-content: center;
}

.menu-toggle:hover {
  background: rgba(255, 255, 255, 0.25);
}

.menu-icon {
  line-height: 1;
}

.brand {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.brand-icon {
  font-size: 24px;
}

.brand-title {
  font-size: 20px;
  font-weight: 600;
  letter-spacing: -0.5px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

@media (max-width: 768px) {
  .menu-toggle {
    display: flex;
  }
  
  .brand-title {
    font-size: 16px;
  }
}
</style>
