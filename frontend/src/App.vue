<template>
  <div id="app">
    <header class="header">
      <h1>📁 文件管理器</h1>
    </header>
    
    <div class="app-container">
      <aside class="sidebar">
        <div class="toolbar">
          <button class="btn" @click="showCreateDialog(true)">📁 新建目录</button>
          <button class="btn" @click="showCreateDialog(false)">📄 新建文件</button>
        </div>
        <TreeView
          ref="treeViewRef"
          :selected-path="selectedPath"
          @select="handleSelect"
        />
      </aside>
      
      <main class="main-content">
        <div class="content-toolbar" v-if="selectedFile">
          <span class="current-file">{{ selectedFile.name }}</span>
          <div class="toolbar-actions">
            <button 
              v-if="isEditable" 
              class="btn"
              :class="{ 'btn-primary': isEditing }"
              @click="toggleEdit"
            >
              {{ isEditing ? '预览' : '编辑' }}
            </button>
          </div>
        </div>
        
        <div class="content-area">
          <FileEditor
            v-if="isEditing && selectedFile"
            ref="editorRef"
            :path="selectedFile.path"
            @saved="handleSaved"
            @change="handleEditorChange"
          />
          <FilePreview
            v-else-if="selectedFile"
            :path="selectedFile.path"
            :extension="selectedFile.extension"
          />
          <div v-else class="empty-state">
            <p>👈 选择一个文件进行预览或编辑</p>
          </div>
        </div>
      </main>
    </div>

    <CreateDialog
      v-model:visible="createDialogVisible"
      :is-directory="createIsDirectory"
      :parent-path="currentParentPath"
      @created="handleCreated"
    />
    
    <ConfirmDialog
      v-model:visible="confirmDialogVisible"
      title="未保存的更改"
      message="您有未保存的更改，确定要离开吗？"
      confirm-text="离开"
      cancel-text="取消"
      @confirm="confirmLeave"
    />
    
    <Toast ref="toastRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { FileNode } from '@/types'
import TreeView from '@/components/TreeView.vue'
import FilePreview from '@/components/preview/FilePreview.vue'
import FileEditor from '@/components/FileEditor.vue'
import CreateDialog from '@/components/CreateDialog.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import Toast from '@/components/Toast.vue'
import { isEditable as checkEditable } from '@/utils/fileIcons'

const treeViewRef = ref<InstanceType<typeof TreeView> | null>(null)
const editorRef = ref<InstanceType<typeof FileEditor> | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const selectedFile = ref<FileNode | null>(null)
const selectedPath = computed(() => selectedFile.value?.path || '')
const isEditing = ref(false)
const hasUnsavedChanges = ref(false)

const createDialogVisible = ref(false)
const createIsDirectory = ref(true)
const confirmDialogVisible = ref(false)
const pendingAction = ref<(() => void) | null>(null)

const currentParentPath = computed(() => {
  if (!selectedFile.value) return '/'
  if (selectedFile.value.type === 'directory') return selectedFile.value.path
  const parts = selectedFile.value.path.split('/')
  parts.pop()
  return parts.join('/') || '/'
})

const isEditable = computed(() => {
  return selectedFile.value?.type === 'file' && checkEditable(selectedFile.value.extension)
})

function handleSelect(node: FileNode) {
  if (hasUnsavedChanges.value) {
    pendingAction.value = () => selectNode(node)
    confirmDialogVisible.value = true
    return
  }
  selectNode(node)
}

function selectNode(node: FileNode) {
  selectedFile.value = node
  isEditing.value = false
  hasUnsavedChanges.value = false
}

function toggleEdit() {
  if (isEditing.value && hasUnsavedChanges.value) {
    pendingAction.value = () => { isEditing.value = false }
    confirmDialogVisible.value = true
    return
  }
  isEditing.value = !isEditing.value
}

function handleEditorChange(changed: boolean) {
  hasUnsavedChanges.value = changed
}

function handleSaved() {
  hasUnsavedChanges.value = false
  toastRef.value?.show('文件保存成功', 'success')
}

function showCreateDialog(isDir: boolean) {
  createIsDirectory.value = isDir
  createDialogVisible.value = true
}

function handleCreated() {
  treeViewRef.value?.refresh()
  toastRef.value?.show(createIsDirectory.value ? '目录创建成功' : '文件创建成功', 'success')
}

function confirmLeave() {
  hasUnsavedChanges.value = false
  pendingAction.value?.()
  pendingAction.value = null
}

// 页面离开提示
window.addEventListener('beforeunload', (e) => {
  if (hasUnsavedChanges.value) {
    e.preventDefault()
    e.returnValue = ''
  }
})
</script>

<style scoped>
.content-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  background: #fafafa;
  border-bottom: 1px solid #e0e0e0;
}

.current-file {
  font-weight: 500;
  color: #333;
}

.content-area {
  flex: 1;
  overflow: hidden;
}

.empty-state {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 16px;
}
</style>
