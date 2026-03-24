<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useMusicStore } from '../store/music.js';
import SongList from '../components/SongList.vue';
import { DeleteOutlined } from '@ant-design/icons-vue';
import { Modal } from 'ant-design-vue';
// 导入IndexedDB Hook
import { useMusicSheetsDB } from '../composables/useMusicSheetsDB.js';
// 导入消息提示组件
import { message } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

const route = useRoute();
const musicStore = useMusicStore();
const { t } = useI18n();

// 初始化IndexedDB Hook
const { getSheetById, clearSheetSongs } = useMusicSheetsDB();

// 歌单详情
const playlist = ref(null);
// 加载状态
const loading = ref(false);
const isEnd = ref(false);
const currentPage = ref(1);
// 每页显示的歌曲数量
const pageSize = ref(20);

// 计算当前歌单
const currentSheet = computed(() => {
  return musicStore.musicSheets.find(sheet => sheet.id === Number(route.params.id));
});

// 监听当前歌单变化，自动更新播放列表
watch(
  currentSheet,
  newSheet => {
    if (newSheet) {
      playlist.value = newSheet.musicList;
    }
  },
  { deep: true }
);

// 获取歌单详情
const getPlaylistDetail = async () => {
  const sheetId = route.params.id;
  if (!sheetId) return;

  loading.value = true;
  try {
    const sheet = await getSheetById(sheetId);
    if (sheet) {
      playlist.value = sheet.musicList;
      // 设置当前歌单到Pinia store
      musicStore.setCurrentSheet(sheet);
    }
  } catch (error) {
    console.error('获取歌单详情失败:', error);
  } finally {
    loading.value = false;
  }
};

// 初始化时获取歌单详情
onMounted(() => {
  getPlaylistDetail();
});

watch(() => route.params.id, getPlaylistDetail);

// 播放全部歌曲
const playAll = () => {
  if (playlist.value?.length > 0) {
    musicStore.setPlaylist(playlist.value, 0);
    playSong(playlist.value[0], 0);
  }
};

// 播放歌曲
const playSong = (song, index) => {
  musicStore.playSong(song);
};

// 添加到播放列表
const addToPlaylist = item => {
  if (item) {
    musicStore.addToPlaylist(item, 0);
    message.success(
      `${t('musicSheetDetail.addSuccess')}${playlist.value.length}${t('musicSheetDetail.songs')}`
    );
  } else {
    musicStore.addSongsToPlaylist(playlist.value || [], 0);
    message.success(
      t('musicSheetDetail.addSuccessSong', {
        count: playlist.value.length,
      })
    );
  }
};

// 从播放列表移除歌曲
const removeSong = (song, index) => {
  musicStore.removeSong(song, index);
};

// 清空播放列表
const clearPlaylist = () => {
  Modal.confirm({
    title: t('musicSheetDetail.confirmClearTitle'),
    content: t('musicSheetDetail.confirmClearContent'),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    center: true,
    onOk: async () => {
      try {
        await clearSheetSongs(route.params.id);

        // 更新Pinia store中的歌单列表
        const { getAllSheets } = useMusicSheetsDB();
        const sheets = await getAllSheets();
        musicStore.setMusicSheets(sheets);

        // 更新本地播放列表显示
        playlist.value = [];
        message.success(t('musicSheetDetail.clearSuccess'));
      } catch (error) {
        console.error('清空歌单失败:', error);
        Modal.error({
          title: t('common.error'),
          content: t('musicSheetDetail.clearError'),
        });
      }
    },
  });
};
</script>

<template>
  <div class="playlist-page main-detail-container">
    <div class="playlist-detail main-detail-content">
      <div class="playlist-actions">
        <a-button class="mr12" type="primary" @click="playAll">{{ $t('common.playAll') }}</a-button>
        <a-button class="mr12" type="primary" @click="addToPlaylist()">{{
          $t('common.addToPlaylist')
        }}</a-button>
        <a-button @click="clearPlaylist">{{ $t('common.clearPlaylist') }}</a-button>
      </div>
      <div class="playlist-content">
        <SongList
          :listSongs="playlist || []"
          :isEnd="true"
          :currentPage="currentPage"
          :isShowAdd="true"
          :isShowDelete="false"
        >
        </SongList>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 加载状态 */
.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
  font-size: 18px;
  color: #888;
}

/* 歌单详情 */
.playlist-detail {
  height: 100%;
  overflow: auto;
}
.playlist-actions {
  margin-bottom: 8px;
}
.playlist-content {
  height: calc(100% - 40px);
}
/* 歌单头部 */
.playlist-header {
  display: flex;
  gap: 20px;
  padding: 20px;
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  margin-bottom: 30px;
}

.header-cover {
  position: relative;
  width: 200px;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
}

.header-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.play-button {
  position: absolute;
  bottom: 15px;
  right: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background-color: rgba(24, 144, 255, 0.9);
  border-radius: 20px;
  cursor: pointer;
  transition:
    background-color 0.3s,
    transform 0.3s;
}

.play-button:hover {
  background-color: rgba(64, 169, 255, 0.9);
  transform: scale(1.05);
}

.play-button img {
  width: 18px;
  height: 18px;
}

.play-button span {
  font-size: 14px;
  font-weight: 500;
  color: #fff;
}

.header-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 12px;
}

.info-type {
  font-size: 14px;
  color: #1890ff;
  font-weight: 500;
}

.info-name {
  font-size: 28px;
  font-weight: bold;
  color: #fff;
}

.info-description {
  font-size: 14px;
  color: #888;
  line-height: 1.6;
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  line-clamp: 3;
  -webkit-box-orient: vertical;
}

.info-stats {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #888;
}

.play-count {
  display: flex;
  align-items: center;
  gap: 6px;
}

.play-count img {
  width: 16px;
  height: 16px;
}

/* 歌曲列表 */
.song-list {
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  overflow: hidden;
}

.song-header {
  display: flex;
  padding: 12px 20px;
  background-color: rgba(255, 255, 255, 0.1);
  font-size: 14px;
  color: #888;
  font-weight: 500;
}

.header-number {
  width: 80px;
  text-align: center;
}

.header-title {
  flex: 1;
  text-align: left;
}

.header-artist {
  width: 150px;
  text-align: left;
}

.header-duration {
  width: 80px;
  text-align: right;
}

.song-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.song-item:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.song-number {
  position: relative;
  width: 80px;
  text-align: center;
  font-size: 16px;
  color: #888;
}

.song-actions {
  display: none;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  gap: 10px;
}

.song-item:hover .song-actions {
  display: flex;
}

.song-item:hover .song-number span {
  display: none;
}

.song-actions img {
  width: 20px;
  height: 20px;
  cursor: pointer;
  transition: opacity 0.3s;
}

.song-actions img:hover {
  opacity: 0.8;
}

.song-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.song-name {
  font-size: 14px;
  font-weight: 500;
  color: #fff;
}

.song-artist {
  width: 150px;
  font-size: 14px;
  color: #888;
  text-align: left;
}

.song-duration {
  width: 80px;
  text-align: right;
  font-size: 12px;
  color: #888;
}

/* 歌单不存在 */
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
  font-size: 18px;
  color: #888;
}
</style>
