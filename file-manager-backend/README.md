# 文件管理器后端

基于 Spring Boot 的文件管理系统后端服务，提供 RESTful API 接口。

## 技术栈

- Java 17
- Spring Boot 3.2.0
- Maven
- Lombok
- Apache POI (Word/Excel 处理)

## 项目结构

```
backend/
├── src/main/java/com/filemanager/
│   ├── FileManagerApplication.java    # 启动类
│   ├── config/
│   │   └── CorsConfig.java            # CORS 跨域配置
│   ├── controller/
│   │   └── FileController.java        # REST API 控制器
│   ├── dto/
│   │   ├── ApiResponse.java           # 通用响应对象
│   │   ├── FileContentDTO.java        # 文件内容 DTO
│   │   └── FileNodeDTO.java           # 文件节点 DTO
│   ├── exception/
│   │   ├── FileOperationException.java    # 文件操作异常
│   │   └── GlobalExceptionHandler.java    # 全局异常处理
│   ├── service/
│   │   ├── FileService.java           # 文件服务接口
│   │   └── FileServiceImpl.java       # 文件服务实现
│   └── util/
│       ├── FileTypeUtil.java          # 文件类型工具
│       └── PathValidator.java         # 路径验证工具
└── src/main/resources/
    └── application.yml                # 配置文件
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+

### 运行项目

```bash
# 进入后端目录
cd backend

# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run
```

服务将在 `http://localhost:8080` 启动。

### 配置说明

在 `application.yml` 中可配置：

```yaml
file-manager:
  root-path: ./managed-files    # 文件管理根目录
  max-file-size: 10485760       # 最大文件大小 (10MB)
```

## API 接口

### 1. 获取目录树

```
GET /api/files/tree?path={path}
```

**参数：**
- `path`: 目录路径，默认为 `/`

**响应示例：**
```json
{
  "success": true,
  "data": {
    "name": "root",
    "path": "/",
    "type": "directory",
    "children": [...]
  }
}
```

### 2. 获取文件内容

```
GET /api/files/content?path={path}
```

**参数：**
- `path`: 文件路径

**响应示例：**
```json
{
  "success": true,
  "data": {
    "path": "/readme.md",
    "content": "# Hello",
    "encoding": "UTF-8",
    "mimeType": "text/markdown"
  }
}
```

### 3. 保存文件内容

```
PUT /api/files/content
```

**请求体：**
```json
{
  "path": "/readme.md",
  "content": "# Updated Content"
}
```

### 4. 创建目录

```
POST /api/files/directory
```

**请求体：**
```json
{
  "path": "/new-folder"
}
```

### 5. 创建文件

```
POST /api/files/file
```

**请求体：**
```json
{
  "path": "/docs/new-file.md",
  "content": ""
}
```

### 6. 检查文件是否存在

```
GET /api/files/exists?path={path}
```

## 错误码

| 错误码 | 描述 |
|--------|------|
| FILE_NOT_FOUND | 文件不存在 |
| DIRECTORY_NOT_FOUND | 目录不存在 |
| PERMISSION_DENIED | 权限不足 |
| INVALID_PATH | 无效路径 |
| PATH_TRAVERSAL | 路径遍历攻击 |
| FILE_EXISTS | 文件已存在 |
| INVALID_NAME | 无效的文件/目录名 |

## 安全特性

- 路径遍历攻击防护
- 文件名验证
- CORS 跨域配置
