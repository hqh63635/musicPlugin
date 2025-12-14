<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import api from '../services/api.js';
import { useMusicStore } from '../store/music.js';
import SongList from '../components/SongList.vue';
import { DeleteOutlined } from '@ant-design/icons-vue';

const route = useRoute();
const musicStore = useMusicStore();

// 歌单详情
const playlist = ref(null);
// 加载状态
const loading = ref(false);
const isEnd = ref(false);
const currentPage = ref(1);
// 每页显示的歌曲数量
const pageSize = ref(20);

// 获取完整的歌曲列表
const fullSongList = computed(() => {
  return musicStore?.playlist || [];
});

// 计算当前页的歌曲列表（本地分页）
const songList = computed(() => {
  const startIndex = (currentPage.value - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;
  return fullSongList.value.slice(0, endIndex);
});

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(fullSongList.value.length / pageSize.value);
});

// 监听当前页变化，确保不超过总页数
const handlePageChange = page => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
    isEnd.value = page >= totalPages.value;
  }
};

// 初始化时检查是否为最后一页
onMounted(() => {
  handlePageChange(1);
});

// 播放全部歌曲
const playAll = () => {
  if (playlist.value?.songs?.length > 0) {
    musicStore.setPlaylist(playlist.value.songs, 0);
  }
};

// 播放歌曲
const playSong = (song, index) => {
  musicStore.playSong(song);
};

// 添加到播放列表
const addToPlaylist = song => {
  musicStore.addToPlaylist(song);
};

// 从播放列表移除歌曲
const removeSong = (song, index) => {
  musicStore.removeSong(song, index);
};
</script>

<template>
  <div class="playlist-page main-detail-container">
    <div class="playlist-detail main-detail-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading">加载中...</div>
      <SongList
        :listSongs="songList || []"
        :isEnd="isEnd"
        :currentPage="currentPage"
        @pageChange="handlePageChange"
        :isShowAdd="false"
        :isShowDelete="true"
      >
      </SongList>
    </div>

    <!-- 歌单不存在 -->
    <!-- <div v-else class="not-found">
      <p>歌单不存在或已被删除</p>
    </div> -->
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
  -webkit-line-clamp: 3;
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
