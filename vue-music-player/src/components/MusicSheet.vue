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
      <div
        class="playlist-item"
        v-for="item in musicStore.musicSheets"
        :key="item.id"
        :class="{ 'is-selected': +route.params.id === item.id }"
      >
        <div class="playlist-content" @click="goToMusicSheetDetail(item.id)">
          <div class="playlist-cover">
            <CdIcon alt="歌单封面" />
          </div>
          <div class="playlist-info">
            <div class="playlist-name">{{ item.name }}</div>
            <div class="playlist-count">{{ item.trackCount }}首</div>
          </div>
        </div>
        <div class="playlist-actions">
          <TrashIcon class="trash-icon" alt="删除歌单" @click.stop="showDeleteConfirm(item)" />
        </div>
      </div>
    </div>
    <a-modal
      v-model:visible="visible"
      title="导入歌单"
      maskClosable="false"
      @cancel="visible = false"
      @ok="handleImport"
      okText="导入"
      cancelText="取消"
      centered
    >
      <div class="modal-content">
        <div class="modal-body">
          <div class="modal-text">请输入歌单链接或ID</div>
          <a-form :model="importForm" :rules="importRules" ref="importFormRef">
            <a-form-item name="url" :label="null">
              <a-input v-model:value="importForm.url" placeholder="请输入歌单链接或ID" />
            </a-form-item>
          </a-form>
        </div>
      </div>
    </a-modal>
    <a-modal
      v-model:visible="createVisible"
      title="创建歌单"
      :maskClosable="false"
      @cancel="closeCreateSheetModal"
      @ok="handleCreateSheet"
      okText="创建"
      cancelText="取消"
      centered
    >
      <div class="modal-content">
        <div class="modal-body">
          <div class="modal-text">请输入歌单名称</div>
          <a-form :model="formData" :rules="rules" ref="formRef">
            <a-form-item name="name" :label="null">
              <a-input v-model:value="formData.name" placeholder="请输入歌单名称" />
            </a-form-item>
          </a-form>
        </div>
      </div>
    </a-modal>

    <a-modal
      v-model:visible="selectSheetVisible"
      title="选择歌单"
      maskClosable="false"
      @cancel="closeSelectSheetModal"
      @ok="handleAddToSelectedSheet"
      okText="添加到所选歌单"
      cancelText="取消"
      centered
    >
      <div class="modal-content">
        <div class="modal-body">
          <div class="modal-text">请选择要添加到的歌单</div>
          <a-button type="primary" @click="handleCreateNewSheet"> 创建新歌单 </a-button>
          <div class="playlist-select-list">
            <div
              v-for="item in musicStore.musicSheets"
              :key="item.id"
              class="playlist-select-item"
              :class="{ 'is-selected': selectedSheetId === item.id }"
              @click="selectedSheetId = item.id"
            >
              <div class="playlist-select-name">{{ item.name }}</div>
              <div class="playlist-select-count">{{ item.trackCount }}首</div>
            </div>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
// 导入ant-design-vue组件
import { Modal } from 'ant-design-vue';
// 导入SVG图标
import CdIcon from '@/assets/icons/cd.svg';
import DocumentPlusIcon from '@/assets/icons/document-plus.svg';
import arrowleftendonrectangle from '@/assets/icons/arrow-left-end-on-rectangle.svg';
import TrashIcon from '@/assets/icons/trash.svg';
// 导入API方法
import { importMusicSheet } from '../services/api.js';
// 导入IndexedDB Hook
import { useMusicSheetsDB } from '../composables/useMusicSheetsDB.js';
// 导入Pinia状态管理
import { useMusicStore } from '../store/music.js';

// 路由实例
const router = useRouter();
const route = useRoute();

// 初始化Pinia store
const musicStore = useMusicStore();

// 初始化IndexedDB Hook
const { getAllSheets, saveSheet, addSongsToSheet, deleteSheet } = useMusicSheetsDB();

/* ---------------- 弹窗状态 ---------------- */
const visible = ref(false);
const createVisible = ref(false);
const selectSheetVisible = ref(false);

/* ---------------- 导入 ---------------- */
const loading = ref(false);
const tempImportedSongs = ref([]);

/* 导入表单验证 */
const importFormRef = ref(null);
const importForm = reactive({
  url: 'https://i.y.qq.com/n2/m/share/details/taoge.html?hosteuin=7wvAoKoP7KvA&id=9596296646&appversion=140805&ADTAG=wxfshare&appshare=iphone_wx',
});
const importRules = {
  url: [
    { required: true, message: '请输入歌单链接或ID', trigger: 'blur' },
    { type: 'string', message: '请输入有效的歌单链接或ID', trigger: 'blur' },
  ],
};

/* ---------------- 创建歌单 ---------------- */
const creating = ref(false);

/* 表单验证 */
const formRef = ref(null);
const formData = reactive({
  name: '',
});
const rules = {
  name: [
    { required: true, message: '请输入歌单名称', trigger: 'blur' },
    { min: 1, max: 20, message: '歌单名称长度在1-20个字符之间', trigger: 'blur' },
    {
      pattern: /^[^\\/:*?"<>|]+$/,
      message: '歌单名称不能包含特殊字符(\\/:*?"<>|)',
      trigger: 'blur',
    },
  ],
};

/* ---------------- 歌单 ---------------- */
// 使用Pinia store中的歌单列表
const selectedSheetId = ref(null);

/* ========================================================================
   业务逻辑
   ======================================================================== */

/* 导入歌单 */
const handleImport = async () => {
  // 表单验证
  await importFormRef.value.validate();

  loading.value = true;
  try {
    const list = await importMusicSheet(importForm.url.trim());
    tempImportedSongs.value = list || [];

    visible.value = false;
    selectSheetVisible.value = true;

    // 重置表单
    if (importFormRef.value) {
      importFormRef.value.resetFields();
    }
  } catch (error) {
    // 如果是表单验证错误，会自动显示，这里处理其他错误
    if (error.message && error.message !== 'Validation failed') {
      Modal.error({
        title: '导入失败',
        content: '导入歌单时发生错误，请确保链接有效并稍后重试。',
      });
    }
  } finally {
    loading.value = false;
  }
};

/* 添加到已存在歌单 */
const handleAddToSelectedSheet = async () => {
  await addSongsToSheet(Number(selectedSheetId.value), tempImportedSongs.value);

  // 更新Pinia store中的歌单列表
  const sheets = await getAllSheets();
  musicStore.setMusicSheets(sheets);

  tempImportedSongs.value = [];
  selectedSheetId.value = null;
  selectSheetVisible.value = false;
};

/* 选择新建歌单 */
const handleCreateNewSheet = () => {
  selectSheetVisible.value = false;
  createVisible.value = true;
};

/* 创建歌单并写入 songlist */
const handleCreateSheet = async () => {
  // 表单验证
  await formRef.value.validate();

  creating.value = true;
  try {
    await saveSheet({
      name: formData.name.trim(),
      createTime: Date.now(),
      musicList: JSON.parse(JSON.stringify(tempImportedSongs.value)),
      trackCount: tempImportedSongs.value.length,
    });

    // 更新Pinia store中的歌单列表
    const sheets = await getAllSheets();
    musicStore.setMusicSheets(sheets);
    tempImportedSongs.value = [];

    // 重置表单
    formData.name = '';
    if (formRef.value) {
      formRef.value.resetFields();
    }

    createVisible.value = false;
  } catch (error) {
    // 如果是表单验证错误，会自动显示，这里处理其他错误
    if (error.message && error.message !== 'Validation failed') {
      Modal.error({
        title: '创建失败',
        content: '创建歌单时发生错误，请稍后重试。',
      });
    }
  } finally {
    creating.value = false;
  }
};

/* 显示删除确认对话框 */
const showDeleteConfirm = item => {
  Modal.confirm({
    title: '确认删除歌单',
    content: `确定要删除歌单「${item.name}」吗？删除后将无法恢复。`,
    okText: '确定',
    cancelText: '取消',
    center: true,
    onOk() {
      handleDeleteSheet(item.id);
    },
  });
};

/* 删除歌单 */
const handleDeleteSheet = async sheetId => {
  try {
    await deleteSheet(sheetId);
    // 更新Pinia store中的歌单列表
    const sheets = await getAllSheets();
    musicStore.setMusicSheets(sheets);
  } catch (error) {
    console.error('删除歌单失败:', error);
    Modal.error({
      title: '删除失败',
      content: '删除歌单时发生错误，请稍后重试。',
    });
  }
};

/* 跳转到歌单详情页面 */
const goToMusicSheetDetail = id => {
  selectedSheetId.value = id;
  router.push(`/musicsheet/${id}`);
};

// 关闭创建歌单弹窗
const closeCreateSheetModal = () => {
  createVisible.value = false;
  formData.name = '';
};

// 关闭选择歌单弹窗
const closeSelectSheetModal = () => {
  selectSheetVisible.value = false;
  selectedSheetId.value = null;
};

/* 初始化 */
onMounted(async () => {
  const sheets = await getAllSheets();
  musicStore.setMusicSheets(sheets);
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
  color: var(--theme-text-primary);
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
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.playlist-content {
  display: flex;
  gap: 12px;
  flex: 1;
}

.playlist-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.playlist-item:hover .playlist-actions {
  opacity: 1;
}

.trash-icon {
  width: 16px;
  height: 16px;
  cursor: pointer;
  opacity: 0.6;
  transition: opacity 0.3s, color 0.3s;
}

.trash-icon:hover {
  opacity: 1;
  color: var(--theme-accent-primary);
}

.playlist-item:hover {
  background-color: var(--theme-bg-hover);
}

.playlist-item.is-selected {
  background-color: var(--theme-bg-secondary);
  color: var(--theme-accent-primary);
}

.playlist-cover {
  width: 48px;
  height: 48px;
  background-color: var(--theme-bg-secondary);
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
  color: var(--theme-text-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.playlist-count {
  font-size: 12px;
  color: var(--theme-text-secondary);
}

/* 导入歌单模态框样式 */
.modal-text {
  margin-bottom: 10px;
  font-size: 14px;
  color: var(--theme-text-primary);
}

.error-message {
  margin-top: 10px;
  font-size: 12px;
  color: var(--theme-accent-primary);
}

/* 选择歌单弹窗样式 */
.playlist-select-list {
  max-height: 300px;
  overflow-y: auto;
  margin-top: 10px;
}

.playlist-select-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s, border-color 0.3s;
  margin-bottom: 8px;
}

.playlist-select-item:hover {
  background-color: var(--theme-bg-hover);
}

.playlist-select-item.is-selected {
  background-color: var(--theme-bg-secondary);
  border: 1px solid var(--theme-accent-primary);
}

.playlist-select-name {
  font-size: 14px;
  color: var(--theme-text-primary);
}

.playlist-select-count {
  font-size: 12px;
  color: var(--theme-text-secondary);
}
</style>
