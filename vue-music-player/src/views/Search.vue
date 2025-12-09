<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import api from '../services/api.js';
import SongList from '../components/SongList.vue';

const route = useRoute();
const keyword = ref('');
const searchResults = ref([]);
const loading = ref(false);
const currentPage = ref(1);
const isEnd = ref(false);

// 当路由参数变化时重新搜索
onMounted(() => {
  keyword.value = route.query.keyword || '';
  if (keyword.value) {
    performSearch();
  }
});

watch(
  () => route.query.keyword,
  newValue => {
    keyword.value = newValue || '';
    if (newValue) {
      performSearch();
    }
  }
);
// 执行搜索
const performSearch = async () => {
  if (!keyword.value) return;

  loading.value = true;
  try {
    const result = await api.search(keyword.value, currentPage.value, 'music');
    if (currentPage.value === 1) {
       searchResults.value = result.data;
    }else {
      searchResults.value.push(...result.data);
    }
    isEnd.value = result.isEnd;
  } catch (error) {
    console.error('搜索失败:', error);
  } finally {
    loading.value = false;
  }
};

const handlePageChange = (page) => {
  currentPage.value = page;
  performSearch();
};

// 格式化歌曲时长
const formatDuration = seconds => {
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = seconds % 60;
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
};
</script>

<template>
  <div class="artist-detail-container">
    <div class="artist-detail-content">
      <h2>搜索结果: {{ keyword }}</h2>

      <div v-if="loading" class="loading">搜索中...</div>

      <div v-else-if="searchResults.length === 0" class="no-results">没有找到相关结果</div>

      <div v-if="searchResults.length" class="search-results">
        <SongList
          :listSongs="searchResults"
          :isEnd="isEnd"
          :currentPage="currentPage"
          @pageChange="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

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
  height: calc(100% - 55px);
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
</style>
