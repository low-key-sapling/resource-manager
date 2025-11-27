<template>
  <div class="pdf-preview">
    <div class="pdf-message">
      <p>📕 PDF 文件预览</p>
      <p class="path">{{ path }}</p>
      <p class="hint">PDF 预览需要额外配置 PDF.js 库</p>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  path: string
}>()
</script>

<style scoped>
.pdf-preview {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.pdf-message {
  text-align: center;
  color: #666;
}

.pdf-message p {
  margin: 8px 0;
}

.pdf-message .path {
  font-family: monospace;
  font-size: 12px;
  color: #999;
}

.pdf-message .hint {
  font-size: 12px;
  color: #999;
}
</style>
