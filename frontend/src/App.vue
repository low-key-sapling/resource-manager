<template>
  <div id="app">
    <AppHeader 
      title="文件管理器" 
      :sidebar-collapsed="sidebarCollapsed"
      @toggle-menu="toggleSidebar"
      @settings="settingsDialogVisible = true"
    />
    
    <div class="app-container">
      <div 
        class="sidebar-overlay" 
        :class="{ visible: !sidebarCollapsed && isMobile }"
        @click="closeSidebar"
      ></div>
      
      <aside class="sidebar" :class="{ collapsed: sidebarCollapsed, open: !sidebarCollapsed && isMobile }">
        <div class="toolbar">
          <button class="btn" @click="showCreateDialog(true)">
            <span>📁</span> 新建目录
          </button>
          <button class="btn" @click="showCreateDialog(false)">
            <span>📄</span> 新建文件
          </button>
        </div>
        <TreeView
          ref="treeViewRef"
          :selected-path="selectedPath"
          @select="handleSelect"
        />
      </aside>
      
      <main class="main-content">
        <div class="content-toolbar" v-if="selectedFile || selectedDirectory">
          <Breadcrumb 
            :path="currentPath" 
            @navigate="handleBreadcrumbNavigate"
          />
          <div class="toolbar-actions">
            <template v-if="selectedFile?.type === 'file'">
              <button 
                v-if="isEditable" 
                class="btn"
                :class="{ 'btn-primary': isEditing }"
                @click="toggleEdit"
              >
                {{ isEditing ? '📖 预览' : '✏️ 编辑' }}
              </button>
            </template>
          </div>
        </div>
        
        <div class="content-area">
          <DirectoryView
            v-if="selectedDirectory && !selectedFile"
            :path="selectedDirectory.path"
            :files="selectedDirectory.children || []"
            @select="handleDirectoryItemSelect"
            @navigate="handleDirectoryNavigate"
          />
          
          <FileEditor
            v-else-if="isEditing && selectedFile"
            ref="editorRef"
            :path="selectedFile.path"
            :extension="selectedFile.extension"
            @saved="handleSaved"
            @change="handleEditorChange"
          />
          
          <FilePreview
            v-else-if="selectedFile"
            :path="selectedFile.path"
            :extension="selectedFile.extension"
          />
          
          <div v-else class="empty-state">
            <div class="empty-icon">📂</div>
            <p class="empty-text">选择一个文件或目录开始浏览</p>
            <p class="empty-hint">点击左侧目录树中的项目</p>
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
    
    <SettingsDialog
      v-model:visible="settingsDialogVisible"
      @saved="handleSettingsSaved"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import type { FileNode } from '@/types'
import AppHeader from '@/components/AppHeader.vue'
import TreeView from '@/components/TreeView.vue'
import DirectoryView from '@/components/DirectoryView.vue'
import Breadcrumb from '@/components/Breadcrumb.vue'
import FilePreview from '@/components/preview/FilePreview.vue'
import FileEditor from '@/components/FileEditor.vue'
import CreateDialog from '@/components/CreateDialog.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import Toast from '@/components/Toast.vue'
import SettingsDialog from '@/components/SettingsDialog.vue'
import { isEditable as checkEditable } from '@/utils/fileIcons'

const treeViewRef = ref<InstanceType<typeof TreeView> | null>(null)
const editorRef = ref<InstanceType<typeof FileEditor> | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const sidebarCollapsed = ref(false)
const isMobile = ref(false)

const selectedFile = ref<FileNode | null>(null)
const selectedDirectory = ref<FileNode | null>(null)
const selectedPath = computed(() => selectedFile.value?.path || selectedDirectory.value?.path || '')
const currentPath = computed(() => selectedFile.value?.path || selectedDirectory.value?.path || '/')

const isEditing = ref(false)
const hasUnsavedChanges = ref(false)

const createDialogVisible = ref(false)
const createIsDirectory = ref(true)
const confirmDialogVisible = ref(false)
const settingsDialogVisible = ref(false)
const pendingAction = ref<(() => void) | null>(null)

const currentParentPath = computed(() => {
  if (selectedDirectory.value) return selectedDirectory.value.path
  if (!selectedFile.value) return '/'
  const parts = selectedFile.value.path.split('/')
  parts.pop()
  return parts.join('/') || '/'
})

const isEditable = computed(() => {
  return selectedFile.value?.type === 'file' && checkEditable(selectedFile.value.extension)
})

function checkMobile() {
  isMobile.value = window.innerWidth < 768
  if (!isMobile.value) {
    sidebarCollapsed.value = false
  }
}

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

function closeSidebar() {
  if (isMobile.value) {
    sidebarCollapsed.value = true
  }
}

function handleSelect(node: FileNode) {
  if (hasUnsavedChanges.value) {
    pendingAction.value = () => selectNode(node)
    confirmDialogVisible.value = true
    return
  }
  selectNode(node)
}

function selectNode(node: FileNode) {
  if (node.type === 'directory') {
    selectedDirectory.value = node
    selectedFile.value = null
  } else {
    selectedFile.value = node
  }
  isEditing.value = false
  hasUnsavedChanges.value = false
  
  if (isMobile.value) {
    sidebarCollapsed.value = true
  }
}

function handleDirectoryItemSelect(node: FileNode) {
  if (node.type === 'file') {
    selectedFile.value = node
    isEditing.value = false
  } else if (node.type === 'directory') {
    // 双击目录时进入该目录
    selectedDirectory.value = node
    selectedFile.value = null
  }
}

function handleDirectoryNavigate(path: string) {
  treeViewRef.value?.navigateToPath(path)
}

function handleBreadcrumbNavigate(path: string) {
  treeViewRef.value?.navigateToPath(path)
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

function handleSettingsSaved() {
  // 根目录更改后刷新目录树
  selectedFile.value = null
  selectedDirectory.value = null
  treeViewRef.value?.refresh()
  toastRef.value?.show('根目录已更新', 'success')
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

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
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
  min-height: 48px;
}

.toolbar-actions {
  display: flex;
  gap: var(--spacing-sm);
}

.content-area {
  flex: 1;
  overflow: hidden;
  background: var(--bg-secondary);
}

.empty-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  padding: var(--spacing-xl);
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: var(--spacing-md);
  opacity: 0.5;
}

.empty-text {
  font-size: 18px;
  color: var(--text-secondary);
  margin-bottom: var(--spacing-sm);
}

.empty-hint {
  font-size: 14px;
}

.sidebar-overlay {
  display: none;
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 99;
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.sidebar-overlay.visible {
  display: block;
  opacity: 1;
}
</style>
