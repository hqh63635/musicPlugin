<template>
  <!-- 歌单列表 -->
  <div class="playlist-section">
    <div class="section-header">
      <span>我的歌单</span>
      <span>
        <arrowleftendonrectangle class="mr8" alt="导入" @click="visible = true" />
        <DocumentPlusIcon alt="创建" @click="createVisible = true" />
      </span>
    </div>
    <div class="playlist-list">
      <div class="playlist-item" v-for="item in musicsSheetList" :key="item.id">
        <div class="playlist-cover">
          <CdIcon alt="歌单封面" />
        </div>
        <div class="playlist-info">
          <div class="playlist-name">{{ item.name }}</div>
          <div class="playlist-count">{{ item.trackCount }}首</div>
        </div>
      </div>
    </div>
    <a-modal v-model:visible="visible" title="导入歌单">
      <div class="modal-content">
        <div class="modal-header">
          <div class="modal-title">导入歌单</div>
        </div>
        <div class="modal-body">
          <div class="modal-text">请输入歌单链接或ID</div>
          <a-input v-model:value="importUrl" placeholder="请输入歌单链接或ID" />
          <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
        </div>
        <div class="modal-footer">
          <a-button @click="visible = false">取消</a-button>
          <a-button type="primary" @click="handleImport" :loading="loading"> 导入 </a-button>
        </div>
      </div>
    </a-modal>
    <a-modal v-model:visible="createVisible" title="创建歌单">
      <div class="modal-content">
        <div class="modal-header">
          <div class="modal-title">创建歌单</div>
        </div>
        <div class="modal-body">
          <div class="modal-text">请输入歌单名称</div>
          <a-input v-model:value="newSheetName" placeholder="请输入歌单名称" />
          <div v-if="createError" class="error-message">{{ createError }}</div>
        </div>
        <div class="modal-footer">
          <a-button @click="createVisible = false">取消</a-button>
          <a-button type="primary" @click="handleCreateSheet" :loading="creating"> 创建 </a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
// 导入SVG图标
import CdIcon from '@/assets/icons/cd.svg';
import DocumentPlusIcon from '@/assets/icons/document-plus.svg';
import arrowleftendonrectangle from '@/assets/icons/arrow-left-end-on-rectangle.svg';
// 导入API方法
import { importMusicSheet } from '../services/api.js';

const visible = ref(false);
const importUrl = ref(
  'https://i.y.qq.com/n2/m/share/details/taoge.html?hosteuin=7wvAoKoP7KvA&id=9596296646&appversion=140805&ADTAG=wxfshare&appshare=iphone_wx'
);
const loading = ref(false);
const errorMessage = ref('');

// 创建歌单相关变量
const createVisible = ref(false);
const newSheetName = ref('');
const creating = ref(false);
const createError = ref('');

// 处理导入歌单
const handleImport = async () => {
  if (!importUrl.value.trim()) {
    errorMessage.value = '请输入歌单链接或ID';
    return;
  }

  loading.value = true;
  errorMessage.value = '';

  try {
    const result = await importMusicSheet(importUrl.value.trim());
    console.log('导入歌单结果:', result);
    // 导入成功后可以添加一些逻辑，比如更新歌单列表
    visible.value = false;
    importUrl.value = '';
  } catch (error) {
    console.error('导入歌单失败:', error);
    errorMessage.value = '导入失败，请检查链接是否正确';
  } finally {
    loading.value = false;
  }
};

// IndexedDB操作方法
const DB_NAME = 'MusicSheetsDB';
const DB_VERSION = 1;
const STORE_NAME = 'sheets';

// 初始化数据库
const initDB = () => {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION);

    request.onupgradeneeded = event => {
      const db = event.target.result;
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        db.createObjectStore(STORE_NAME, { keyPath: 'id', autoIncrement: true });
      }
    };

    request.onsuccess = event => {
      resolve(event.target.result);
    };

    request.onerror = event => {
      reject(event.target.error);
    };
  });
};

// 获取所有歌单
const getAllSheets = async () => {
  const db = await initDB();
  const result = [];

  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_NAME, 'readonly');
    const store = tx.objectStore(STORE_NAME);
    const request = store.openCursor();

    request.onsuccess = e => {
      const cursor = e.target.result;
      if (cursor) {
        result.push(cursor.value);
        cursor.continue();
      } else {
        resolve(result);
      }
    };

    request.onerror = () => reject(request.error);
    tx.onerror = () => reject(tx.error);
    tx.oncomplete = () => db.close();
  });
};

// 添加或更新歌单
const addSheet = async sheet => {
  const db = await initDB();

  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_NAME, 'readwrite');
    const store = tx.objectStore(STORE_NAME);

    // put：存在则更新，不存在则新增
    const request = store.put(sheet);

    request.onsuccess = () => {
      resolve(request.result);
    };

    request.onerror = () => {
      reject(request.error);
    };

    tx.onerror = () => {
      reject(tx.error);
    };

    tx.oncomplete = () => {
      db.close();
    };
  });
};

// 处理创建歌单
const handleCreateSheet = async () => {
  if (!newSheetName.value.trim()) {
    createError.value = '请输入歌单名称';
    return;
  }

  creating.value = true;
  createError.value = '';

  try {
    const newSheet = {
      name: newSheetName.value.trim(),
      createTime: new Date().toISOString(),
      trackCount: 0,
      musicList: [],
    };

    await addSheet(newSheet);
    createVisible.value = false;
    newSheetName.value = '';

    // 重新加载歌单列表
    musicsSheetList.value = await getAllSheets();
  } catch (error) {
    console.error('创建歌单失败:', error);
    createError.value = '创建歌单失败，请重试';
  } finally {
    creating.value = false;
  }
};

const musicsSheetList = ref([]);

// 组件初始化时加载歌单列表
onMounted(async () => {
  try {
    musicsSheetList.value = await getAllSheets();
  } catch (error) {
    console.error('初始化加载歌单失败:', error);
  }
});
</script>

<style scoped>
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header span {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.section-header svg {
  width: 18px;
  height: 18px;
  opacity: 0.6;
  cursor: pointer;
  transition: opacity 0.3s;
}

.section-header svg:hover {
  opacity: 1;
}
/* 歌单列表 */
.playlist-section {
  flex: 1;
  padding: 12px;
}
.playlist-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.playlist-item {
  display: flex;
  gap: 12px;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.playlist-item:hover {
  background-color: #f5f5f5;
}

.playlist-cover {
  width: 48px;
  height: 48px;
  background-color: #f0f0f0;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.playlist-cover svg {
  width: 24px;
  height: 24px;
  opacity: 0.6;
}

.playlist-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
}

.playlist-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.playlist-count {
  font-size: 12px;
  color: #999;
}

/* 导入歌单模态框样式 */
.modal-text {
  margin-bottom: 10px;
  font-size: 14px;
  color: #666;
}

.error-message {
  margin-top: 10px;
  font-size: 12px;
  color: #ff4d4f;
}
</style>
