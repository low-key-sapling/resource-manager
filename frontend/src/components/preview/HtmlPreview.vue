<template>
  <div class="html-preview">
    <iframe 
      ref="iframeRef"
      sandbox="allow-same-origin"
      :srcdoc="content"
    ></iframe>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

defineProps<{
  content: string
}>()

const iframeRef = ref<HTMLIFrameElement | null>(null)
</script>

<style scoped>
.html-preview {
  height: 100%;
  padding: 0;
}

.html-preview iframe {
  width: 100%;
  height: 100%;
  border: none;
  background: white;
}
</style>
