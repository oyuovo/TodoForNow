# JustTodo Windows 打包说明（方案一：本机测试）

## 说明

- 打包后的 exe 会**自动启动本地后端**，用户无需手动运行后端
- 本机需预装：**Java 17**、**PostgreSQL**（后端使用）
- 确保 PostgreSQL 已创建数据库并配置好 `JustTodoBE` 的 `application.yaml`

## 打包步骤

1. **安装依赖**
   ```bash
   cd TodoWeb
   npm install
   ```

2. **执行打包**
   ```bash
   npm run build:win
   ```
   会自动执行：`mvn package`（构建后端 jar）→ `vite build`（构建前端）→ `electron-builder`（打包 exe）

3. **输出位置**
   - `TodoWeb/release/win-unpacked/` 目录下：
     - `JustTodo.exe`：直接运行此 exe 即可（无需安装）

## 运行流程

1. 用户双击 exe
2. 应用自动启动内嵌的 Spring Boot 后端（`--spring.profiles.active=prod`）
3. 等待后端就绪后打开窗口
4. 关闭应用时自动结束后端进程

## 后端配置

- 打包时使用 `application-prod.yaml`，配置了 `server.servlet.context-path=/api`，与前端请求路径一致
- 数据库等配置沿用 `application.yaml`，需在本机提前配置好 PostgreSQL
