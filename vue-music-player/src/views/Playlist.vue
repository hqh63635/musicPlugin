<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../services/api.js'
import { useMusicStore } from '../store/music.js'

const route = useRoute()
const musicStore = useMusicStore()

// 歌单详情
const playlist = ref(null)
// 加载状态
const loading = ref(false)

// 页面加载时获取歌单详情
onMounted(() => {
  fetchPlaylistDetail()
})

// 获取歌单详情
const fetchPlaylistDetail = async () => {
  try {
    loading.value = true
    const result = await api.getPlaylistInfo(route.params.id)
    if (result.success) {
      playlist.value = result.data
    }
  } catch (error) {
    console.error('获取歌单详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 播放全部歌曲
const playAll = () => {
  if (playlist.value?.songs?.length > 0) {
    musicStore.setPlaylist(playlist.value.songs, 0)
  }
}

// 播放歌曲
const playSong = (song, index) => {
  musicStore.playSong(song)
}

// 添加到播放列表
const addToPlaylist = (song) => {
  musicStore.addToPlaylist(song)
}
</script>

<template>
  <div class="playlist-page">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading">加载中...</div>
    
    <!-- 歌单详情 -->
    <div v-else-if="playlist" class="playlist-detail">
      <!-- 歌单头部 -->
      <div class="playlist-header">
        <div class="header-cover">
          <img :src="playlist.cover" :alt="playlist.name" />
          <div class="play-button" @click="playAll">
            <img src="@/assets/icons/play.svg" alt="播放全部" />
            <span>播放全部</span>
          </div>
        </div>
        <div class="header-info">
          <div class="info-type">歌单</div>
          <h1 class="info-name">{{ playlist.name }}</h1>
          <div class="info-description">{{ playlist.description || '暂无描述' }}</div>
          <div class="info-stats">
            <span class="play-count">
              <img src="@/assets/icons/play.svg" alt="播放" />
              {{ playlist.playCount > 10000 ? (playlist.playCount / 10000).toFixed(1) + '万' : playlist.playCount }}
            </span>
            <span class="song-count">{{ playlist.songs?.length || 0 }}首歌曲</span>
          </div>
        </div>
      </div>
      
      <!-- 歌曲列表 -->
      <div class="song-list">
        <div class="song-header">
          <div class="header-number">#</div>
          <div class="header-title">标题</div>
          <div class="header-artist">歌手</div>
          <div class="header-duration">时长</div>
        </div>
        
        <div 
          v-for="(song, index) in playlist.songs" 
          :key="song.id"
          class="song-item"
        >
          <!-- 序号 -->
          <div class="song-number">
            <span>{{ index + 1 }}</span>
            <div class="song-actions">
              <img 
                src="@/assets/icons/play.svg" 
                alt="播放" 
                @click="playSong(song, index)"
              />
              <img 
                src="@/assets/icons/plus.svg" 
                alt="添加" 
                @click="addToPlaylist(song)"
              />
            </div>
          </div>
          
          <!-- 歌曲信息 -->
          <div class="song-info">
            <h3 class="song-name">{{ song.name }}</h3>
          </div>
          
          <!-- 歌手信息 -->
          <div class="song-artist">
            {{ song.singer?.[0]?.name || song.artist?.[0] || '未知歌手' }}
          </div>
          
          <!-- 歌曲时长 -->
          <div class="song-duration">
            {{ musicStore.formatTime(song.duration) }}
          </div>
        </div>
      </div>
    </div>
    
    <!-- 歌单不存在 -->
    <div v-else class="not-found">
      <p>歌单不存在或已被删除</p>
    </div>
  </div>
</template>

<style scoped>
.playlist-page {
  padding: 20px;
  color: #fff;
}

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
  max-width: 1200px;
  margin: 0 auto;
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
  transition: background-color 0.3s, transform 0.3s;
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