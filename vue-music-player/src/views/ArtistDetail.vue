<template>
  <div class="artist-detail-container main-detail-container">
    <div class="artist-detail-content main-detail-content">
      <a-spin :spinning="loading" :indicator="indicator" class="loading-spin">
        <div class="artist-info">
          <img
            :src="singer?.avatar || singer?.artwork || '@/assets/default-avatar.jpg'"
            :alt="artistInfo?.name"
            class="artist-avatar"
          />
          <div class="artist-details">
            <p>
              <strong>{{ $t('artistDetail.biography') }}:</strong>
              {{ singer?.name || singer?.title || $t('common.unknown') }}
            </p>
            <p>
              <strong>{{ $t('artistDetail.region') }}:</strong>
              {{ singer?.area || singer?.artist || $t('common.unknown') }}
            </p>
            <p>
              <strong>{{ $t('artistDetail.genre') }}:</strong>
              {{ singer?.name || $t('common.unknown') }}
            </p>
          </div>
          <div class="play-active">
            <a-button class="mr-12" type="primary" @click="playAllSongs">{{
              $t('artistDetail.playAll')
            }}</a-button>
            <a-button type="primary" @click="addToPlaylist()">{{
              $t('artistDetail.addToPlaylist')
            }}</a-button>
          </div>
        </div>
        <div class="songs-list">
          <SongList
            :listSongs="artistInfo || []"
            :isEnd="isEnd"
            :currentPage="currentPage"
            @pageChange="handlePageChange"
            :isShowAdd="true"
            :isShowDelete="false"
          >
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
import SongList from '@/components/SongList.vue';
import { useMusicStore } from '../store/music.js';
import { PlusCircleOutlined, LoadingOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

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
    message.success(`$(item.title)添加到播放列表`);
  } else {
    musicStore.addSongsToPlaylist(artistInfo.value || [], 0);
    message.success(
      `${t('artistDetail.addSuccessBulk')}${artistInfo.value.length}${t('common.songs')}`
    );
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
  height: calc(100vh - 120px);
  overflow: auto;
}

.artist-detail-content {
  height: 100%;
}

.loading,
.error {
  text-align: center;
  padding: 40px;
  font-size: 18px;
}

.error {
  color: var(--theme-accent-primary);
  transition: color 0.3s;
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
  color: var(--theme-text-secondary);
  transition: color 0.3s;
}

.songs-list {
  height: calc(100% - 120px);
  overflow: auto;
}

.song-item {
  padding: 10px 0;
  border-bottom: 1px solid var(--theme-border-primary);
  cursor: pointer;
  transition: border-bottom-color 0.3s;
  transition: background-color 0.2s;
}

.song-item:hover {
  background-color: var(--theme-bg-hover);
}

.no-songs {
  text-align: center;
  padding: 20px;
  color: var(--theme-text-tertiary);
  transition: color 0.3s;
}
.mr-12 {
  margin-right: 12px;
}
</style>
