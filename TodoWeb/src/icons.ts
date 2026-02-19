/**
 * 图标常量，统一管理项目图标
 * 使用 Iconify 图标集：https://icon-sets.iconify.design/
 * 图标格式：前缀:图标名，如 mdi:plus、lucide:trash-2
 */
export const icons = {
  /** 添加 / 新建 */
  add: 'lucide:plus',
  /** 编辑 / 重命名 */
  edit: 'lucide:pencil',
  /** 删除 */
  delete: 'lucide:trash-2',
  /** 清除 / 清空 */
  clear: 'lucide:trash-2',
  /** 保存 */
  save: 'lucide:save',
  /** 文件夹 */
  folder: 'lucide:folder',
  /** 打开文件夹 */
  folderOpen: 'lucide:folder-open',
  /** 设置 / 路径 */
  settings: 'lucide:settings',
  /** 编辑模式 */
  editMode: 'lucide:edit-3',
  /** 浏览 / 阅读模式 */
  viewMode: 'lucide:eye',
  /** 用户 / 个人 */
  user: 'lucide:user',
  /** 待办 / 清单 */
  list: 'lucide:list-todo',
  /** 备忘录 */
  memo: 'lucide:file-text',
  /** 深色模式 */
  moon: 'lucide:moon',
  /** 浅色模式 */
  sun: 'lucide:sun',
  /** 定时 / 闹钟 */
  clock: 'lucide:clock',
} as const;
