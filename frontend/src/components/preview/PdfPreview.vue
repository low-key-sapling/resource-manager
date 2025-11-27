<template>
  <div class="pdf-preview">
    <div class="pdf-toolbar">
      <a 
        :href="downloadUrl" 
        target="_blank" 
        class="btn btn-sm"
        title="在新窗口打开"
      >
        🔗 新窗口打开
      </a>
      <a 
        :href="downloadUrl" 
        download 
        class="btn btn-sm"
        title="下载PDF"
      >
        ⬇️ 下载
      </a>
    </div>
    
    <div class="pdf-container">
      <iframe 
        :src="downloadUrl"
        class="pdf-iframe"
        frameborder="0"
      ></iframe>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  path: string
}>()

const downloadUrl = computed(() => {
  return `/api/files/download?path=${encodeURIComponent(props.path)}`
})
</script>

<style scoped>
.pdf-preview {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--bg-tertiary);
}

.pdf-toolbar {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
  gap: var(--spacing-md);
}

.pdf-container {
  flex: 1;
  overflow: hidden;
}

.pdf-iframe {
  width: 100%;
  height: 100%;
  border: none;
}
</style>
