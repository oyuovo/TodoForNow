const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('memoFs', {
  list: (basePath) => ipcRenderer.invoke('memo-fs:list', basePath),
  read: (basePath, id) => ipcRenderer.invoke('memo-fs:read', basePath, id),
  write: (basePath, id, content) => ipcRenderer.invoke('memo-fs:write', basePath, id, content),
  create: (basePath, title) => ipcRenderer.invoke('memo-fs:create', basePath, title),
  delete: (basePath, id) => ipcRenderer.invoke('memo-fs:delete', basePath, id),
  openFolder: (folderPath) => ipcRenderer.invoke('memo-fs:openFolder', folderPath),
});
