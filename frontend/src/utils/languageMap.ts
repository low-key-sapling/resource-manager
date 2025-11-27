/**
 * 文件扩展名到Monaco Editor语言标识的映射
 */
const extensionToLanguage: Record<string, string> = {
  // JavaScript/TypeScript
  'js': 'javascript',
  'jsx': 'javascript',
  'ts': 'typescript',
  'tsx': 'typescript',
  'mjs': 'javascript',
  'cjs': 'javascript',
  
  // Web
  'html': 'html',
  'htm': 'html',
  'css': 'css',
  'scss': 'scss',
  'sass': 'scss',
  'less': 'less',
  'vue': 'html',
  
  // Data formats
  'json': 'json',
  'xml': 'xml',
  'yaml': 'yaml',
  'yml': 'yaml',
  'toml': 'ini',
  
  // Programming languages
  'java': 'java',
  'py': 'python',
  'rb': 'ruby',
  'php': 'php',
  'go': 'go',
  'rs': 'rust',
  'c': 'c',
  'cpp': 'cpp',
  'cc': 'cpp',
  'h': 'c',
  'hpp': 'cpp',
  'cs': 'csharp',
  'swift': 'swift',
  'kt': 'kotlin',
  'kts': 'kotlin',
  'scala': 'scala',
  
  // Shell/Scripts
  'sh': 'shell',
  'bash': 'shell',
  'zsh': 'shell',
  'ps1': 'powershell',
  'bat': 'bat',
  'cmd': 'bat',
  
  // Database
  'sql': 'sql',
  
  // Markup/Documentation
  'md': 'markdown',
  'markdown': 'markdown',
  'tex': 'latex',
  
  // Config files
  'ini': 'ini',
  'conf': 'ini',
  'cfg': 'ini',
  'properties': 'ini',
  'env': 'ini',
  
  // Other
  'dockerfile': 'dockerfile',
  'graphql': 'graphql',
  'gql': 'graphql',
  'r': 'r',
  'lua': 'lua',
  'perl': 'perl',
  'pl': 'perl'
}

/**
 * 根据文件扩展名获取Monaco Editor语言标识
 */
export function getLanguageByExtension(extension?: string): string {
  if (!extension) return 'plaintext'
  const ext = extension.toLowerCase()
  return extensionToLanguage[ext] || 'plaintext'
}

/**
 * 根据文件名获取Monaco Editor语言标识
 */
export function getLanguageByFilename(filename: string): string {
  const lowerName = filename.toLowerCase()
  
  // 特殊文件名处理
  if (lowerName === 'dockerfile') return 'dockerfile'
  if (lowerName === 'makefile') return 'makefile'
  if (lowerName === '.gitignore' || lowerName === '.dockerignore') return 'ini'
  if (lowerName === '.env' || lowerName.startsWith('.env.')) return 'ini'
  
  // 从文件名提取扩展名
  const parts = filename.split('.')
  if (parts.length > 1) {
    const ext = parts[parts.length - 1]
    return getLanguageByExtension(ext)
  }
  
  return 'plaintext'
}

/**
 * 获取所有支持的语言列表
 */
export function getSupportedLanguages(): string[] {
  return [...new Set(Object.values(extensionToLanguage))]
}
