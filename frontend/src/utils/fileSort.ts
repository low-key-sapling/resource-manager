import type { FileNode } from '@/types'

/**
 * 排序文件节点：目录优先，然后按字母顺序
 */
export function sortFileNodes(nodes: FileNode[]): FileNode[] {
  return [...nodes].sort((a, b) => {
    // 目录优先
    if (a.type === 'directory' && b.type !== 'directory') {
      return -1
    }
    if (a.type !== 'directory' && b.type === 'directory') {
      return 1
    }
    // 同类型按字母顺序
    return a.name.toLowerCase().localeCompare(b.name.toLowerCase())
  })
}

/**
 * 递归排序整个文件树
 */
export function sortFileTree(node: FileNode): FileNode {
  if (node.type === 'directory' && node.children) {
    return {
      ...node,
      children: sortFileNodes(node.children).map(sortFileTree)
    }
  }
  return node
}
