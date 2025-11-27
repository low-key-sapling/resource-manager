import axios, { AxiosInstance, AxiosError } from 'axios'
import type { ApiResponse, FileNode, FileContent, CreateRequest, SaveRequest } from '@/types'

const api: AxiosInstance = axios.create({
  baseURL: '/api/files',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 响应拦截器
api.interceptors.response.use(
  response => response,
  (error: AxiosError<ApiResponse<unknown>>) => {
    const message = error.response?.data?.message || error.message || '请求失败'
    return Promise.reject(new Error(message))
  }
)

/**
 * 文件 API 服务
 */
export const fileApi = {
  /**
   * 获取目录树
   */
  async getTree(path: string = '/'): Promise<FileNode> {
    const response = await api.get<ApiResponse<FileNode>>('/tree', { params: { path } })
    if (!response.data.success) {
      throw new Error(response.data.message || '获取目录树失败')
    }
    return response.data.data!
  },

  /**
   * 获取文件内容
   */
  async getContent(path: string): Promise<FileContent> {
    const response = await api.get<ApiResponse<FileContent>>('/content', { params: { path } })
    if (!response.data.success) {
      throw new Error(response.data.message || '获取文件内容失败')
    }
    return response.data.data!
  },

  /**
   * 保存文件内容
   */
  async saveContent(request: SaveRequest): Promise<void> {
    const response = await api.put<ApiResponse<void>>('/content', request)
    if (!response.data.success) {
      throw new Error(response.data.message || '保存文件失败')
    }
  },

  /**
   * 创建目录
   */
  async createDirectory(path: string): Promise<void> {
    const response = await api.post<ApiResponse<void>>('/directory', { path })
    if (!response.data.success) {
      throw new Error(response.data.message || '创建目录失败')
    }
  },

  /**
   * 创建文件
   */
  async createFile(request: CreateRequest): Promise<void> {
    const response = await api.post<ApiResponse<void>>('/file', request)
    if (!response.data.success) {
      throw new Error(response.data.message || '创建文件失败')
    }
  },

  /**
   * 检查文件是否存在
   */
  async exists(path: string): Promise<boolean> {
    const response = await api.get<ApiResponse<boolean>>('/exists', { params: { path } })
    if (!response.data.success) {
      throw new Error(response.data.message || '检查文件失败')
    }
    return response.data.data!
  }
}

export default fileApi
