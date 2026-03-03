# JustTodo / My_CoDe 总览

## 架构概览

- **前端 TodoWeb**
  - 技术栈：Vue 3 + TypeScript + Vite + Electron。
  - 主要职责：提供待办清单、备忘录、个人信息等 UI；在桌面环境下通过 Electron 与本地文件系统交互。
- **后端 JustTodoBE**
  - 技术栈：Spring Boot 3 + MyBatis-Plus + PostgreSQL + Redis。
  - 主要职责：账号登录、待办清单与待办项管理、备忘录根路径配置、登录态与缓存管理、定时任务。

## 本地开发启动方式

### 前端 TodoWeb

1. 安装依赖：
   ```bash
   cd TodoWeb
   npm install
   ```
2. 启动 Web 开发服务器：
   ```bash
   npm run dev
   ```
3. 启动 Electron 桌面版开发环境：
   ```bash
   npm run electron:dev
   ```

### 后端 JustTodoBE

1. 确保本地安装：
   - Java 17+
   - Maven 3.8+
   - PostgreSQL
   - Redis
2. 复制配置模板并修改实际配置：
   ```bash
   cd JustTodoBE/src/main/resources
   cp application.yaml.example application.yaml
   # 按实际数据库、Redis、jwt.secret 等修改 application.yaml
   ```
3. 在项目根目录启动：
   ```bash
   cd ../../..
   mvn spring-boot:run
   ```

## 关键功能模块

- **登录与认证**
  - 使用数字 ID + 密码登录，密码采用 BCrypt 存储。
  - 登录成功后生成 JWT，并将 token 写入 Redis（`auth:token:{token}`），后续通过拦截器统一校验，失败时返回统一 JSON 结构。

- **待办清单与待办项**
  - 每个用户可拥有多条清单，每条清单下有多个待办项。
  - 支持创建、重命名、删除清单，新增/修改/删除待办项，清除已完成待办。
  - 使用 Redis 缓存整个用户的清单+待办列表，并通过定时任务与缓存失效机制保持一致。

- **备忘录**
  - 桌面版使用本地 `.md` 文件存储备忘录内容，并可自定义根目录。
  - Web 环境下使用内存数据兜底。

- **定时任务**
  - 每天 0 点将 `timeset=3` 的定时任务重置为 `timeset=1`，并清理相关 Redis 缓存，使前端能看到最新状态。

- **桌面通知与每日概览**
  - 在 Electron 桌面环境下，使用系统通知展示“今日待办概览”（普通待办数 + 定时任务数）。
  - 同一天内只推送一次概览通知，避免频繁打扰（需要操作系统允许应用发送通知）。

- **基础统计面板**
  - 提供独立的“统计面板”视图，展示当前用户的清单总数、待办总数、普通/定时任务数量等。
  - 计算并展示定时任务完成率（已完成定时任务 / 全部定时任务），帮助用户了解定时任务执行情况。

## 构建与打包

- 前端 Electron Windows 安装包：
  ```bash
  cd TodoWeb
  npm run build:win
  ```
  该脚本会：
  - 先在 `JustTodoBE` 中执行 `mvn package` 打包后端；
  - 构建前端静态资源；
  - 使用 electron-builder 生成 Windows 目录安装包，并将后端 Jar 一并打入资源目录。

