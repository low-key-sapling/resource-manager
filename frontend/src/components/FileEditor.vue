<template>
  <div class="file-editor">
    <!-- Markdown编辑器 -->
    <MarkdownEditor
      v-if="isMarkdown"
      ref="markdownEditorRef"
      :path="path"
      @saved="$emit('saved')"
      @change="handleChange"
    />
    
    <!-- 代码编辑器 -->
    <CodeEditor
      v-else
      ref="codeEditorRef"
      :path="path"
      :extension="extension"
      @saved="$emit('saved')"
      @change="handleChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import MarkdownEditor from './MarkdownEditor.vue'
import CodeEditor from './CodeEditor.vue'

const props = defineProps<{
  path: string
  extension?: string
}>()

const emit = defineEmits<{
  (e: 'saved'): void
  (e: 'change', hasChanges: boolean): void
}>()

const markdownEditorRef = ref<InstanceType<typeof MarkdownEditor> | null>(null)
const codeEditorRef = ref<InstanceType<typeof CodeEditor> | null>(null)

const isMarkdown = computed(() => {
  return props.extension?.toLowerCase() === 'md' || props.extension?.toLowerCase() === 'markdown'
})

const hasChanges = computed(() => {
  if (isMarkdown.value) {
    return markdownEditorRef.value?.hasChanges ?? false
  }
  return codeEditorRef.value?.hasChanges ?? false
})

function handleChange(changed: boolean) {
  emit('change', changed)
}

async function save() {
  if (isMarkdown.value) {
    await markdownEditorRef.value?.save()
  } else {
    await codeEditorRef.value?.save()
  }
}

function checkUnsavedChanges(): boolean {
  return hasChanges.value
}

defineExpose({ save, checkUnsavedChanges, hasChanges })
</script>

<style scoped>
.file-editor {
  height: 100%;
  display: flex;
  flex-direction: column;
}
</style>
