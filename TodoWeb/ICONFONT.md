# 图标使用说明

项目使用 [Iconify](https://iconify.design/) 作为图标库，提供丰富的图标选择。

## 当前使用方式

- **组件**：`AppIcon`（`src/components/AppIcon.vue`）
- **图标常量**：`src/icons.ts` 统一管理
- **图标格式**：`前缀:图标名`，如 `lucide:plus`、`lucide:trash-2`

## 使用示例

```vue
<AppIcon :icon="icons.add" :size="16" />
<AppIcon icon="lucide:heart" :size="20" />
```

## 切换为 iconfont.cn（阿里图标库）

若希望使用 [iconfont.cn](https://www.iconfont.cn/)：

1. 在 iconfont.cn 创建项目，添加所需图标
2. 选择 **Symbol** 方式，下载到本地
3. 将 `iconfont.js` 放入 `src/assets/iconfont/`
4. 在 `main.ts` 中引入：`import './assets/iconfont/iconfont.js'`
5. 使用方式：`<svg class="icon"><use xlink:href="#icon-xxx"></use></svg>`

可新建 `IconfontIcon.vue` 组件封装上述用法，并逐步替换 `AppIcon`。
