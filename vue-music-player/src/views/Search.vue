<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../services/api.js'

const route = useRoute()
const keyword = ref(route.query.keyword || '')
const searchResults = ref([])
const loading = ref(false)

// 当路由参数变化时重新搜索
onMounted(() => {
  if (keyword.value) {
    performSearch()
  }
})

// 执行搜索
const performSearch = async () => {
  if (!keyword.value) return
  
  loading.value = true
  try {
    const result = await api.search(keyword.value)
    if (result.success) {
      searchResults.value = result.data.songs
    }
  } catch (error) {
    console.error('搜索失败:', error)
  } finally {
    loading.value = false
  }
}

// 格式化歌曲时长
const formatDuration = (seconds) => {
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}
</script>

<template>
  <div class="search-page">
    <h2>搜索结果: {{ keyword }}</h2>
    
    <div v-if="loading" class="loading">
      搜索中...
    </div>
    
    <div v-else-if="searchResults.length === 0" class="no-results">
      没有找到相关结果
    </div>
    
    <div v-else class="search-results">
      <div 
        v-for="song in searchResults" 
        :key="song.id" 
        class="song-item"
      >
        <div class="song-cover">
          <img :src="song.cover" :alt="song.name" />
        </div>
        <div class="song-info">
          <div class="song-name">{{ song.name }}</div>
          <div class="song-artist">{{ song.artist.join(', ') }}</div>
        </div>
        <div class="song-album">{{ song.album }}</div>
        <div class="song-duration">{{ formatDuration(song.duration) }}</div>
        <div class="song-action">
          <img src="@/assets/icons/play.svg" alt="播放" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.search-page {
  padding: 20px;
  color: #fff;
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
  display: flex;
  flex-direction: column;
  gap: 12px;
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
