# 文件管理器

一个基于 Vue 3 + Spring Boot 的全栈文件管理系统，支持文件浏览、预览、创建和编辑功能。

## 功能特性

- 📁 **树形目录浏览** - 以层级结构展示文件和目录
- 👁️ **多格式预览** - 支持 Markdown、HTML、PDF、文本等格式
- ✏️ **在线编辑** - 直接编辑文本类文件
- ➕ **创建管理** - 创建新目录和文件
- 🔒 **安全防护** - 路径遍历攻击防护

## 技术栈

| 前端 | 后端 |
|------|------|
| Vue 3 | Spring Boot 3.2 |
| TypeScript | Java 17 |
| Vite | Maven |
| Axios | Lombok |

## 快速开始

### 1. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端服务运行在 `http://localhost:8080`

### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端应用运行在 `http://localhost:3000`

### 3. 访问应用

打开浏览器访问 `http://localhost:3000`

## 项目结构

```
file-manager/
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/
│   ├── src/main/resources/
│   └── pom.xml
├── frontend/                # Vue 3 前端
│   ├── src/
│   ├── package.json
│   └── vite.config.ts
└── README.md
```

## API 接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/files/tree | 获取目录树 |
| GET | /api/files/content | 获取文件内容 |
| PUT | /api/files/content | 保存文件内容 |
| POST | /api/files/directory | 创建目录 |
| POST | /api/files/file | 创建文件 |
| GET | /api/files/exists | 检查文件是否存在 |

## 支持的文件类型

- **文档**: Markdown, TXT, PDF, Word, Excel
- **代码**: HTML, CSS, JS, TS, JSON, XML, YAML, Java, Python, SQL
- **图片**: PNG, JPG, GIF, SVG (预览)

## 配置

### 后端配置 (application.yml)

```yaml
file-manager:
  root-path: ./managed-files    # 文件管理根目录
```

### 前端配置 (vite.config.ts)

```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080'
    }
  }
}
```

## 开发文档

- [后端 README](./backend/README.md)
- [前端 README](./frontend/README.md)
- [需求文档](./.kiro/specs/file-manager/requirements.md)
- [设计文档](./.kiro/specs/file-manager/design.md)
