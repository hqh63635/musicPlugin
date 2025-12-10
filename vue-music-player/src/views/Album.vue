<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import api from '../services/api.js';
import { useMusicStore } from '../store/music.js';

const route = useRoute();
const router = useRouter();
const musicStore = useMusicStore();

// 艺术家信息
const artist = ref({});
// 艺术家歌曲数据
const songs = ref([]);
// 艺术家专辑数据
const albums = ref([]);

// 新增：歌手分类相关数据
const categories = ref([
  {
    id: '热门',
    name: '热门',
  },
  { id: 'A', name: 'A' },
  { id: 'B', name: 'B' },
  { id: 'C', name: 'C' },
  { id: 'D', name: 'D' },
  { id: 'E', name: 'E' },
  { id: 'F', name: 'F' },
  { id: 'G', name: 'G' },
  { id: 'H', name: 'H' },
  { id: 'I', name: 'I' },
  { id: 'J', name: 'J' },
  { id: 'K', name: 'K' },
  { id: 'L', name: 'L' },
  { id: 'M', name: 'M' },
  { id: 'N', name: 'N' },
  { id: 'O', name: 'O' },
  { id: 'P', name: 'P' },
  { id: 'Q', name: 'Q' },
  { id: 'R', name: 'R' },
  { id: 'S', name: 'S' },
  { id: 'T', name: 'T' },
  { id: 'U', name: 'U' },
  { id: 'V', name: 'V' },
  { id: 'W', name: 'W' },
  { id: 'X', name: 'X' },
  { id: 'Y', name: 'Y' },
  { id: 'Z', name: 'Z' },
]);
const selectedCategory = ref('热门');
const singerList = ref([]);
const isCategoryView = ref(false);
const searchTerm = ref('');
const isEnd = ref(false);
const currentPage = ref(1);
const isLoading = ref(false);
// 检查是否需要自动加载更多（内容未填满视口时）
const checkAndLoadMore = () => {
  const container = document.querySelector('.singer-grid');
  if (!container) return;
  
  const containerHeight = container.offsetHeight;
  const viewportHeight = window.innerHeight;
  
  // 当内容高度小于视口高度且有更多数据时继续加载
  if (containerHeight < viewportHeight - 200 && !isLoading.value && !isEnd.value) {
    fetchSingerList();
  }
};
// 页面加载时初始化
onMounted(() => {
  initPage();
  const observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting && !isLoading.value && !isEnd.value) {
      fetchSingerList();
    }
  });
  const loader = document.createElement('div');
  document.querySelector('.singer-grid').appendChild(loader);
  observer.observe(loader);
  
  // 初始加载后检查是否需要填充内容
  checkAndLoadMore();
  
  return () => observer.disconnect();
});

// 监听路由变化
watch(
  () => route.params.id,
  () => initPage()
);

// 初始化页面状态
const initPage = () => {
  // 判断是否为分类列表模式（无歌手ID参数）
  fetchSingerList();
};

// 获取分类下的歌手列表
const fetchSingerList = async () => {
  if (isLoading.value || isEnd.value) return;
  isLoading.value = true;
  try {
    const searchData = await api.search(selectedCategory.value, currentPage.value, 'album');
    isEnd.value = searchData.isEnd || false;
    const newData = searchData.data || [];
    singerList.value = [...singerList.value, ...newData];
    
    // 只有当有新数据时才增加页码
    if (newData.length > 0) {
      currentPage.value++;
    } else {
      isEnd.value = true;
    }
  } catch (error) {
    console.error('获取歌手列表失败:', error);
  } finally {
    isLoading.value = false;
    // 加载完成后检查是否需要继续填充
    checkAndLoadMore();
  }
};
// 播放歌曲
const playSong = song => {
  musicStore.playSong(song);
};

// 添加到播放列表
const addToPlaylist = song => {
  musicStore.addToPlaylist(song);
};

// 播放全部歌曲
const playAllSongs = () => {
  if (songs.value.length > 0) {
    musicStore.setPlaylist(songs.value, 0);
  }
};

// 新增：切换分类
const changeCategory = key => {
  singerList.value = [];
  currentPage.value = 1;
  fetchSingerList();
};

// 新增：跳转到歌手详情
const goToArtistDetail = singer => {
  router.push(`/artist/${singer?.singerMID}?singer=${encodeURIComponent(JSON.stringify(singer))}`);
};
</script>

<template>
  <div class="artist-page">
    <div class=artist-page-container>
      <!-- 分类视图 -->
    <div class="page-header">
      <h2>歌手分类</h2>
    </div>

    <!-- A-Z分类导航 -->
     <a-radio-group v-model:value="selectedCategory" class="category-nav" @change="changeCategory">
       <a-radio v-for="category in categories" :key="category.id" :value="category.id" class="category-item">
         {{ category.name }}
       </a-radio>
     </a-radio-group>

    <!-- 歌手列表 -->
    <div class="singer-grid">
      <div v-for="singer in singerList" :key="singer.albumMID" class="singer-card" @click="goToArtistDetail(singer)">
        <div class="singer-avatar">
          <img :src="singer.artwork || '@/assets/default-avatar.jpg'" :alt="singer.title" />
        </div>
        <h3 class="singer-name">{{ singer.title }}</h3>
      </div>
      <div v-if="isLoading" class="loading-indicator">加载中...</div>
    </div>
    </div>
  </div>
</template>

<style scoped>
.artist-page {
  padding: 12px;
  color: #333;
  border-radius: 4px;
  background-color: #f5f5f5;
}

/* 艺术家头部信息 */
.artist-header {
  display: flex;
  gap: 20px;
  margin-bottom: 40px;
  padding: 20px;
  background-color: #f8f8f8;
  border-radius: 12px;
}

.artist-avatar {
  width: 180px;
  height: 180px;
  border-radius: 50%;
  overflow: hidden;
  background-color: #e8e8e8;
}

.artist-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.artist-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 12px;
}

.artist-name {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.artist-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.artist-stats {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #999;
}

.artist-actions {
  display: flex;
  gap: 12px;
}

.action-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.action-button.play {
  background-color: #1890ff;
  color: #fff;
}

.action-button.play:hover {
  background-color: #40a9ff;
}

.action-button img {
  width: 16px;
  height: 16px;
}

/* 区块样式 */
.section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

/* 歌曲列表 */
.song-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 0 12px;
}

.song-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  background-color: #f8f8f8;
  cursor: pointer;
  transition: background-color 0.3s;
}

.song-item:hover {
  background-color: #e8e8e8;
}

.song-index {
  width: 30px;
  font-size: 14px;
  color: #999;
  text-align: center;
}

.song-info {
  flex: 1;
  margin-left: 16px;
}

.song-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin: 0 0 4px 0;
}

.song-meta {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.song-actions {
  display: flex;
  gap: 16px;
}

.action-btn {
  width: 32px;
  height: 32px;
  border: none;
  background-color: transparent;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  border-radius: 50%;
  transition: background-color 0.3s;
}

.action-btn:hover {
  background-color: rgba(0, 0, 0, 0.1);
}

.action-btn img {
  width: 16px;
  height: 16px;
}

/* 专辑列表 */
.album-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px;
}

.album-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.album-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.album-cover {
  position: relative;
  width: 100%;
  padding-top: 100%;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 12px;
  background-color: #f5f5f5;
}

.album-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.album-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  opacity: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: opacity 0.3s;
}

.album-card:hover .album-overlay {
  opacity: 1;
}

.overlay-button {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #1890ff;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: transform 0.3s, background-color 0.3s;
}

.overlay-button:hover {
  transform: scale(1.1);
  background-color: #40a9ff;
}

.overlay-button img {
  width: 20px;
  height: 20px;
}

.album-info {
  padding: 0 4px;
}

.album-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.album-date {
  font-size: 12px;
  color: #999;
}

/* 新增：分类视图样式 */
.page-header {
  padding: 0;
}

.category-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  padding: 16px;
  background-color: #f5f5f5;
  border-radius: 8px;
  margin-bottom: 24px;
}

.category-nav > div {
  padding: 6px 12px;
  background-color: #fff;
  border-radius: 16px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.category-nav > div.active {
  background-color: #1890ff;
  color: #fff;
}

.singer-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 20px;
}

.singer-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}

.singer-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  margin-bottom: 12px;
}

.singer-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.singer-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
}
.artist-page-container {
  height: 100%;
  padding: 12px;
  background-color: #fff;
  overflow: auto;
}
</style>










