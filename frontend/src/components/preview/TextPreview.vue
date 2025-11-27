<template>
  <div class="text-preview">
    <pre>{{ content }}</pre>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  content: string
}>()
</script>

<style scoped>
.text-preview {
  padding: 20px;
  height: 100%;
  overflow: auto;
}

.text-preview pre {
  margin: 0;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
