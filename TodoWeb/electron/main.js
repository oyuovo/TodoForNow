const { app, BrowserWindow, ipcMain, shell } = require('electron');
const path = require('path');
const fs = require('fs').promises;

const isDev = process.env.NODE_ENV === 'development' || !app.isPackaged;

function createWindow() {
  const win = new BrowserWindow({
    width: 1200,
    height: 800,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
    },
  });

  if (isDev) {
    win.loadURL('http://localhost:5173');
    win.webContents.openDevTools();
  } else {
    win.loadFile(path.join(__dirname, '../dist/index.html'));
  }
}

app.whenReady().then(createWindow);

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit();
});

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) createWindow();
});

// memoFs 桥接：使用 Node.js fs 读写本地 .md 文件
ipcMain.handle('memo-fs:list', async (_, basePath) => {
  try {
    const dir = path.resolve(basePath);
    const entries = await fs.readdir(dir, { withFileTypes: true });
    const results = [];
    for (const e of entries) {
      if (e.isFile() && e.name.endsWith('.md')) {
        const id = e.name.replace(/\.md$/, '');
        const fullPath = path.join(dir, e.name);
        const content = await fs.readFile(fullPath, 'utf-8').catch(() => '');
        const firstLine = content.split('\n')[0] || '';
        const title = firstLine.startsWith('# ') ? firstLine.slice(2).trim() : id;
        results.push({ id, title });
      }
    }
    return results;
  } catch (err) {
    throw new Error(`读取目录失败: ${err.message}`);
  }
});

ipcMain.handle('memo-fs:read', async (_, basePath, id) => {
  const fullPath = path.join(path.resolve(basePath), `${id}.md`);
  return fs.readFile(fullPath, 'utf-8').catch(() => '');
});

ipcMain.handle('memo-fs:write', async (_, basePath, id, content) => {
  const dir = path.resolve(basePath);
  await fs.mkdir(dir, { recursive: true });
  const fullPath = path.join(dir, `${id}.md`);
  await fs.writeFile(fullPath, content, 'utf-8');
});

ipcMain.handle('memo-fs:create', async (_, basePath, title) => {
  const dir = path.resolve(basePath);
  await fs.mkdir(dir, { recursive: true });
  const id = crypto.randomUUID();
  const fullPath = path.join(dir, `${id}.md`);
  await fs.writeFile(fullPath, `# ${title}\n\n`, 'utf-8');
  return { id };
});

ipcMain.handle('memo-fs:delete', async (_, basePath, id) => {
  const fullPath = path.join(path.resolve(basePath), `${id}.md`);
  await fs.unlink(fullPath).catch(() => {});
});

ipcMain.handle('memo-fs:openFolder', async (_, folderPath) => {
  const resolved = path.resolve(folderPath);
  return shell.openPath(resolved);
});
