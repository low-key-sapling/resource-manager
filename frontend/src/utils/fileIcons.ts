/**
 * 文件图标映射
 */
const iconMap: Record<string, string> = {
  // 目录
  directory: '📁',
  
  // 文档
  md: '📝',
  txt: '📄',
  pdf: '📕',
  doc: '📘',
  docx: '📘',
  xls: '📊',
  xlsx: '📊',
  
  // 代码
  html: '🌐',
  css: '🎨',
  js: '📜',
  ts: '📜',
  json: '📋',
  xml: '📋',
  yaml: '📋',
  yml: '📋',
  
  // 编程语言
  java: '☕',
  py: '🐍',
  sql: '🗃️',
  
  // 图片
  png: '🖼️',
  jpg: '🖼️',
  jpeg: '🖼️',
  gif: '🖼️',
  svg: '🖼️',
  
  // 默认
  default: '📄'
}

/**
 * 根据文件类型获取图标
 */
export function getFileIcon(type: 'file' | 'directory', extension?: string): string {
  if (type === 'directory') {
    return iconMap.directory
  }
  
  if (extension) {
    return iconMap[extension.toLowerCase()] || iconMap.default
  }
  
  return iconMap.default
}

/**
 * 判断文件是否可编辑
 */
export function isEditable(extension?: string): boolean {
  if (!extension) return false
  const editableExtensions = [
    'md', 'txt', 'html', 'css', 'js', 'ts', 'json', 
    'xml', 'yaml', 'yml', 'java', 'py', 'sql'
  ]
  return editableExtensions.includes(extension.toLowerCase())
}

/**
 * 判断文件是否可预览
 */
export function isPreviewable(extension?: string): boolean {
  if (!extension) return false
  const previewableExtensions = [
    'md', 'txt', 'html', 'pdf', 'doc', 'docx', 'xls', 'xlsx',
    'png', 'jpg', 'jpeg', 'gif', 'svg'
  ]
  return previewableExtensions.includes(extension.toLowerCase())
}
