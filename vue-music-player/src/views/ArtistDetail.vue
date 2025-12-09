<template>
  <div class="artist-detail-container">
    <div class="artist-detail-content">
      <h2>{{ artistInfo?.name || '艺术家详情' }}</h2>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="error" class="error">获取艺术家信息失败</div>
      <div v-else class="artist-info">
        <img :src="singer?.avatar" :alt="artistInfo?.name" class="artist-avatar" />
        <div class="artist-details">
          <p><strong>简介:</strong> {{ singer?.name || '暂无简介' }}</p>
          <p><strong>地区:</strong> {{ singer?.area || '未知' }}</p>
          <p><strong>流派:</strong> {{ singer?.name || '未知' }}</p>
        </div>
        <div class="play-active">
          <button @click="playAllSongs">播放全部</button>
        </div>
      </div>
     <div class="songs-list">
          <SongList :listSongs="artistInfo || []" />
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import api from '../services/api';
import SongList from '../components/songList.vue';
import { useMusicStore } from '../store/music.js';

const musicStore = useMusicStore();
const route = useRoute();
const artistId = route.params.id;
// 修复变量声明顺序并添加调试日志
let singer = ref(null);
const singerParam = ref('');
const init = () => {
  console.log('Initializing singer with param:', singerParam.value);
  try {
    singerParam.value = route.query.singer;
    const decoded = decodeURIComponent(singerParam.value);
    console.log('Decoded singer param:', decoded);
    const parsed = JSON.parse(decoded);
    console.log('Parsed singer object:', parsed);
    singer.value = parsed;
    console.log('singer.id value:', singer.value?.id);
  } catch (e) {
    console.error('Failed to parse singer parameter:', e);
    singer.value = null;
  }
};


// 修改加载状态初始化
const artistInfo = ref(null);
const loading = ref(!singer);
const error = ref(false);

const playAllSongs = () => {
  musicStore.setPlaylist(artistInfo.value || [], 0);
};

onMounted(async () => {
  init();
  // 将id检查改为singerMID检查以匹配实际属性名
  if (singer.value?.singerMID) {
    try {
      const response = await api.getArtistWorks(singer.value, 1, 'music');
      artistInfo.value = response.data;
    } catch (err) {
      console.error('获取艺术家详情失败:', err);
      error.value = true;
    } finally {
      loading.value = false;
    }
  }
});
</script>

<style scoped>
.artist-detail-container {
  margin: 0 auto;
  padding: 12px;
  background-color: #f5f5f5;
}
.artist-detail-content {
  height: 100%;
  padding: 12px;
  background-color: #fff;
}
.loading,
.error {
  text-align: center;
  padding: 40px;
  font-size: 18px;
}

.error {
  color: #ff4d4f;
}

.artist-info {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.artist-avatar {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  object-fit: cover;
}

.artist-details {
  flex: 1;
}
.artist-details p {
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

.songs-list {
  height: calc(100% - 140px);
  overflow: auto;
}

.song-item {
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.song-item:hover {
  background-color: #f5f5f5;
}

.no-songs {
  text-align: center;
  padding: 20px;
  color: #999;
}
</style>