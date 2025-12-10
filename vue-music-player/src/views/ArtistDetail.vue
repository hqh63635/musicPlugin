<template>
  <div class="artist-detail-container">
    <div class="artist-detail-content">
      <h2>{{ singer?.name || '艺术家详情' }}</h2>
      <a-spin :spinning="loading" :indicator="indicator" class="loading-spin">
        <div class="artist-info">
          <img :src="singer?.avatar" :alt="artistInfo?.name" class="artist-avatar" />
          <div class="artist-details">
            <p><strong>简介:</strong> {{ singer?.name || '暂无简介' }}</p>
            <p><strong>地区:</strong> {{ singer?.area || '未知' }}</p>
            <p><strong>流派:</strong> {{ singer?.name || '未知' }}</p>
          </div>
          <div class="play-active">
            <a-button class="mr-12" type="primary" @click="playAllSongs">播放全部</a-button>
            <a-button type="primary" @click="addToPlaylist">添加到播放列表</a-button>
          </div>
        </div>
        <div class="songs-list">
          <SongList :listSongs="artistInfo || []" :isEnd="trueß" :currentPage="currentPage" @pageChange="handlePageChange">
            <template #actions="{ item, index }">
              <PlusCircleOutlined style="font-size: 18px; color: #ff4d4f;" @click="addToPlaylist(item, index)" />
            </template>
          </SongList>
        </div>
      </a-spin>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, h } from 'vue';
import { useRoute } from 'vue-router';
import api from '../services/api';
import SongList from '../components/songList.vue';
import { useMusicStore } from '../store/music.js';
import { PlusCircleOutlined, LoadingOutlined } from '@ant-design/icons-vue';
import { Spin } from 'ant-design-vue';

// 添加自定义加载指示器
const indicator = h(LoadingOutlined, { style: { fontSize: '24px' }, spin: true });

const musicStore = useMusicStore();
const route = useRoute();
const artistId = route.params.id;
// 修复变量声明顺序并添加调试日志
let singer = ref(null);
const singerParam = ref('');

const currentPage = ref(1);
const isEnd = ref(false);

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
// Add pagination stat

// 播放全部
const playAllSongs = () => {
  musicStore.playSong(artistInfo.value[0]);
  musicStore.addSongsToPlaylist(artistInfo.value || [], 0);
};

// 添加到播放列表
const addToPlaylist = item => {
  if (item) {
    musicStore.addToPlaylist(item, 0);
  } else {
    musicStore.addSongsToPlaylist(artistInfo.value || [], 0);
  }
};

// Extract API call to reusable function
const fetchArtistWorks = async () => {
  if (singer.value?.singerMID) {
    try {
      loading.value = true;
      const response = await api.getArtistWorks(singer.value, currentPage.value, 'music');
      console.log('API Response:', response);
      isEnd.value = response.isEnd;
      if (currentPage.value === 1) {
        artistInfo.value = response.data;
      } else {
        artistInfo.value.push(...response.data);
      }
      indicator.value.spin = false;
    } catch (err) {
      console.error('获取艺术家详情失败:', err);
      error.value = true;
    } finally {
      loading.value = false;
    }
  }
};

onMounted(async () => {
  init();
  await fetchArtistWorks();
});

const handlePageChange = page => {
  currentPage.value = page;
  fetchArtistWorks();
};

// Watch for currentPage changes to trigger data fetch
watch(currentPage, fetchArtistWorks);
</script>

<style scoped>
/* 添加加载指示器样式 */
.loading-spin {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.artist-detail-container {
  margin: 0 auto;
  padding: 12px;
  background-color: #f5f5f5;
  overflow: auto;
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
.mr-12 {
  margin-right: 12px;
}
</style>