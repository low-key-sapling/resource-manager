# 文件管理器前端

基于 Vue 3 + TypeScript 的文件管理系统前端应用。

## 技术栈

- Vue 3
- TypeScript
- Vite
- Axios
- Marked (Markdown 渲染)

## 项目结构

```
frontend/
├── src/
│   ├── main.ts                    # 入口文件
│   ├── App.vue                    # 主应用组件
│   ├── style.css                  # 全局样式
│   ├── components/
│   │   ├── TreeView.vue           # 树形视图组件
│   │   ├── TreeNode.vue           # 树节点组件
│   │   ├── FileEditor.vue         # 文件编辑器
│   │   ├── CreateDialog.vue       # 创建对话框
│   │   ├── ConfirmDialog.vue      # 确认对话框
│   │   ├── Toast.vue              # 通知组件
│   │   └── preview/
│   │       ├── FilePreview.vue    # 文件预览容器
│   │       ├── MarkdownPreview.vue # Markdown 预览
│   │       ├── TextPreview.vue    # 文本预览
│   │       ├── HtmlPreview.vue    # HTML 预览
│   │       └── PdfPreview.vue     # PDF 预览
│   ├── services/
│   │   └── api.ts                 # API 服务
│   ├── types/
│   │   └── index.ts               # TypeScript 类型定义
│   └── utils/
│       ├── fileIcons.ts           # 文件图标映射
│       └── fileSort.ts            # 文件排序工具
├── index.html
├── package.json
├── tsconfig.json
└── vite.config.ts
```

## 快速开始

### 环境要求

- Node.js 18+
- npm 或 yarn

### 安装依赖

```bash
cd frontend
npm install
```

### 开发模式

```bash
npm run dev
```

应用将在 `http://localhost:3000` 启动。

### 生产构建

```bash
npm run build
```

构建产物将输出到 `dist` 目录。

### 预览构建

```bash
npm run preview
```

## 功能特性

### 文件浏览
- 树形结构展示目录和文件
- 支持展开/折叠目录
- 文件类型图标显示
- 目录优先、字母排序

### 文件预览
- Markdown 渲染预览
- 纯文本预览
- HTML 沙盒预览
- PDF 预览（需配置）
- 代码文件语法高亮

### 文件编辑
- 文本文件在线编辑
- 未保存更改提示
- 离开页面确认
- 保存成功/失败通知

### 文件管理
- 创建新目录
- 创建新文件
- 文件名验证

## 支持的文件类型

### 可预览
- Markdown (.md)
- 文本文件 (.txt)
- HTML (.html, .htm)
- PDF (.pdf)
- 代码文件 (.js, .ts, .css, .json, .xml, .yaml, .java, .py, .sql)

### 可编辑
- 所有文本类文件

## 配置说明

### API 代理

在 `vite.config.ts` 中配置后端 API 代理：

```typescript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

### 路径别名

使用 `@` 作为 `src` 目录的别名：

```typescript
import { fileApi } from '@/services/api'
```

## 组件说明

### TreeView
树形视图组件，用于展示文件目录结构。

```vue
<TreeView
  :selected-path="selectedPath"
  @select="handleSelect"
/>
```

### FilePreview
文件预览组件，根据文件类型自动选择预览器。

```vue
<FilePreview
  :path="filePath"
  :extension="fileExtension"
/>
```

### FileEditor
文件编辑器组件，支持文本编辑和保存。

```vue
<FileEditor
  :path="filePath"
  @saved="handleSaved"
  @change="handleChange"
/>
```

### CreateDialog
创建文件/目录对话框。

```vue
<CreateDialog
  v-model:visible="visible"
  :is-directory="true"
  :parent-path="parentPath"
  @created="handleCreated"
/>
```

## 运行测试

```bash
# 运行测试
npm run test

# 监听模式
npm run test:watch
```
