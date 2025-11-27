/**
 * 文件节点接口
 */
export interface FileNode {
  name: string
  path: string
  type: 'file' | 'directory'
  extension?: string
  size?: number
  children?: FileNode[]
  lastModified?: string
}

/**
 * 文件内容接口
 */
export interface FileContent {
  path: string
  content: string
  encoding: string
  mimeType: string
}

/**
 * API 响应接口
 */
export interface ApiResponse<T> {
  success: boolean
  data?: T
  error?: string
  message?: string
}

/**
 * 创建请求接口
 */
export interface CreateRequest {
  path: string
  content?: string
}

/**
 * 保存请求接口
 */
export interface SaveRequest {
  path: string
  content: string
}
