<script setup>
import { ref, onMounted, watch, h, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import api from '../services/api.js';
import SongList from '../components/SongList.vue';
import { useMusicStore } from '@/store/music.js';
import { PlusCircleOutlined, LoadingOutlined } from '@ant-design/icons-vue';
import { Tabs, TabPane } from 'ant-design-vue';

const musicStore = useMusicStore();
const route = useRoute();
const router = useRouter();
const keyword = ref('');
const searchResults = ref([]);
const loading = ref(false);
const currentPage = ref(1);
const isEnd = ref(false);
const indicator = h(LoadingOutlined, { style: { fontSize: '24px' }, spin: true });
const currentType = ref('music'); // 搜索类型：music, artist, album

// 当路由参数变化时初始化
onMounted(() => {
  keyword.value = route.query.keyword || '';
  currentType.value = route.query.type || 'music'; // 从URL查询参数读取搜索类型
  // 如果URL中有keyword参数，自动执行搜索
  if (route.query.keyword) {
    performSearch();
  }
});

// 监听keyword变化
watch(
  () => route.query.keyword,
  newValue => {
    keyword.value = newValue || '';
    if (newValue) {
      performSearch();
    }
  }
);

// 监听type变化，更新URL查询参数
watch(
  () => currentType.value,
  newValue => {
    router.push(
      {
        path: route.path,
        query: {
          ...route.query,
          type: newValue,
        },
      },
      { replace: true }
    );
  }
);

// 监听URL查询参数中的type变化，保持界面与URL同步
watch(
  () => route.query.type,
  newValue => {
    if (newValue && newValue !== currentType.value) {
      currentType.value = newValue;
      currentPage.value = 1;
      searchResults.value = [];
      performSearch();
    }
  }
);

// 执行搜索
const performSearch = async () => {
  if (!keyword.value || loading.value) return;

  loading.value = true;
  try {
    const result = await api.search(keyword.value, currentPage.value, currentType.value);
    if (currentPage.value === 1) {
      searchResults.value = result.data;
    } else {
      searchResults.value.push(...result.data);
    }
    isEnd.value = result.isEnd;
    // 移除这里的currentPage增加，统一在调用处处理
  } catch (error) {
    console.error('搜索失败:', error);
  } finally {
    loading.value = false;
    if (currentType.value !== 'music') {
      // 加载完成后检查是否需要继续填充
      nextTick(() => {
        checkAndLoadMore();
      });
    }
  }
};

const handlePageChange = page => {
  currentPage.value = page;
  performSearch();
};

const addToPlaylist = (item, index) => {
  musicStore.addToPlaylist(item, 0);
};
// 格式化歌曲时长
const formatDuration = seconds => {
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = seconds % 60;
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
};
// 处理搜索类型切换
const handleTypeChange = key => {
  currentPage.value = 1;
  searchResults.value = [];
  performSearch();
};

// 检查是否需要自动加载更多（内容未填满视口时）
const checkAndLoadMore = () => {
  const container = document.querySelector('.singer-grid');
  if (!container) return;

  const contentHeight = container.scrollHeight; // 使用scrollHeight获取实际内容高度
  const containerHeight = container.offsetHeight;
  const viewportHeight = window.innerHeight;

  // 添加调试日志
  console.log('checkAndLoadMore:', {
    contentHeight,
    containerHeight,
    viewportHeight,
    isEnd: isEnd.value,
    loading: loading.value,
  });

  // 当容器高度小于视口高度、内容高度接近容器高度（表示内容已填满）、有更多数据且没有请求进行中时继续加载
  // 只有当内容确实在增长时才继续加载，避免无限循环
  if (
    containerHeight < viewportHeight - 200 &&
    contentHeight > containerHeight * 0.8 &&
    !isEnd.value &&
    !loading.value
  ) {
    currentPage.value++;
    performSearch();
  }
};
const goToArtistDetail = singer => {
  router.push(`/artist/${singer?.singerMID}?singer=${encodeURIComponent(JSON.stringify(singer))}`);
};

const goToAlbumDetail = album => {
  router.push(`/album/${album?.albumMID}?singer=${encodeURIComponent(JSON.stringify(album))}`);
};

// 处理搜索触发
const handleSearch = () => {
  if (!keyword.value.trim()) return;

  // 重置分页和结果
  currentPage.value = 1;
  searchResults.value = [];

  // 更新URL参数
  router.push({
    path: route.path,
    query: {
      ...route.query,
      keyword: keyword.value,
      type: currentType.value,
    },
  });

  // 执行搜索
  performSearch();
};
</script>

<template>
  <div class="artist-detail-container">
    <div class="artist-detail-content">
      <!-- 搜索框 -->
      <div v-if="!searchResults.length && !loading" class="search-input-container">
        <div class="search-input-wrapper">
          <input
            v-model="keyword"
            type="text"
            placeholder="请输入歌曲、歌手或专辑名称"
            class="search-input"
            @keyup.enter="handleSearch"
          />
          <button @click="handleSearch" class="search-button">搜索</button>
        </div>
      </div>

      <!-- 搜索结果区域 -->
      <div v-if="searchResults.length || loading" class="search-results-section">
        <a-tabs v-model:activeKey="currentType" @change="handleTypeChange" class="search-tabs">
          <a-tab-pane key="music" tab="歌曲"></a-tab-pane>
          <a-tab-pane key="artist" tab="歌手"></a-tab-pane>
          <a-tab-pane key="album" tab="专辑"></a-tab-pane>
        </a-tabs>
        <a-spin :spinning="loading" :indicator="indicator" class="loading-spin">
          <div v-if="searchResults.length" class="search-results">
            <SongList
              v-if="currentType === 'music'"
              :listSongs="searchResults"
              :isEnd="isEnd"
              :currentPage="currentPage"
              @pageChange="handlePageChange"
              :isShowAdd="true"
              :isShowDelete="false"
            >
            </SongList>

            <!-- 歌手列表 -->
            <div class="singer-grid" v-if="currentType === 'artist'">
              <div
                v-for="singer in searchResults"
                :key="singer.singerMID"
                class="singer-card"
                @click="goToArtistDetail(singer)"
              >
                <div class="singer-avatar">
                  <img
                    :src="singer.avatar || singer.artwork || '@/assets/default-avatar.jpg'"
                    :alt="singer.title"
                  />
                </div>
                <h3 class="singer-name">{{ singer.name }}</h3>
              </div>
            </div>

            <!-- 专辑列表 -->
            <div class="singer-grid" v-if="currentType === 'album'">
              <div
                v-for="singer in searchResults"
                :key="singer.albumMID"
                class="singer-card"
                @click="goToAlbumDetail(singer)"
              >
                <div class="singer-avatar">
                  <img
                    :src="singer.avatar || singer.artwork || '@/assets/default-avatar.jpg'"
                    :alt="singer.title"
                  />
                </div>
                <h3 class="singer-name">{{ singer.title }}</h3>
              </div>
            </div>
          </div>
          <div v-else-if="!loading && keyword.value" class="no-results">
            <h3>没有找到相关结果</h3>
            <p>请尝试其他关键词或搜索类型</p>
          </div>
        </a-spin>
      </div>
    </div>
  </div>
</template>

<style scoped>
.search-tabs {
}

/* 搜索框样式 */
.search-input-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 20px;
  min-height: 200px;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 600px;
  border-radius: 24px;
  border: 1px solid #dfe1e5;
  overflow: hidden;
  box-shadow: 0 1px 6px rgba(32, 33, 36, 0.28);
  transition: box-shadow 0.3s;
}

.search-input-wrapper:hover {
  box-shadow: 0 1px 10px rgba(32, 33, 36, 0.35);
}

.search-input {
  flex: 1;
  padding: 12px 20px;
  font-size: 16px;
  border: none;
  outline: none;
  background-color: transparent;
}

.search-button {
  padding: 12px 24px;
  font-size: 16px;
  color: #fff;
  background-color: #1890ff;
  border: none;
  outline: none;
  cursor: pointer;
  transition: background-color 0.3s;
}

.search-button:hover {
  background-color: #40a9ff;
}

.search-button:active {
  background-color: #096dd9;
}

.search-results-section {
  height: 100%;
}
/* 搜索结果区域 */
.artist-detail-container {
  margin: 0 auto;
  padding: 8px;
  background-color: #f5f5f5;
}

.artist-detail-content {
  height: 100%;
  padding: 8px;
  border-radius: 12px;
  background-color: #fff;
}
.loading {
  text-align: center;
  padding: 40px;
  color: #888;
}

.no-results {
  text-align: center;
  padding: 40px;
  color: #888;
}

.search-results {
  height: calc(100% - 35px);
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: auto;
}

.song-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background-color: #2a2a2a;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.song-item:hover {
  background-color: #333;
}

.song-cover {
  width: 56px;
  height: 56px;
  border-radius: 4px;
  overflow: hidden;
}

.song-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.song-info {
  flex: 1;
  min-width: 0;
}

.song-name {
  font-size: 16px;
  color: #fff;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-artist {
  font-size: 14px;
  color: #888;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-album {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  color: #888;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-duration {
  width: 60px;
  text-align: right;
  font-size: 14px;
  color: #888;
}

.song-action {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.song-action img {
  width: 24px;
  height: 24px;
  opacity: 0.7;
  transition: opacity 0.3s;
}

.song-action:hover img {
  opacity: 1;
}
:deep(.ant-spin-nested-loading) {
  height: calc(100% - 35px);
  overflow: auto;
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
</style>
