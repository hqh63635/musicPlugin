<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../services/api.js'
import { useMusicStore } from '../store/music.js'

const route = useRoute()
const musicStore = useMusicStore()

// 专辑信息
const album = ref({
  id: '',
  name: '',
  cover: '',
  artist: '',
  artistId: '',
  releaseDate: '',
  description: '',
  songCount: 0
})

// 专辑歌曲数据
const songs = ref([])

// 页面加载时获取专辑数据
onMounted(() => {
  fetchAlbumData()
})

// 获取专辑数据
const fetchAlbumData = async () => {
  try {
    // 模拟专辑数据
    album.value = {
      id: '002J4UUk29y8BY',
      name: '叶惠美',
      cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000002J4UUk29y8BY_1.jpg',
      artist: '周杰伦',
      artistId: '12345',
      releaseDate: '2003-07-31',
      description: '周杰伦第四张个人专辑《叶惠美》，以母亲叶惠美的名字命名，收录了11首歌曲。',
      songCount: 11
    }

    // 模拟专辑歌曲数据
    songs.value = [
      {
        id: '001JZkTF2XZ8lH',
        name: '晴天',
        artist: '周杰伦',
        album: '叶惠美',
        cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000002J4UUk29y8BY_1.jpg',
        duration: 260
      },
      {
        id: '003a6QZJ0wYrB5',
        name: '三年二班',
        artist: '周杰伦',
        album: '叶惠美',
        cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000002J4UUk29y8BY_1.jpg',
        duration: 240
      },
      {
        id: '004XtDOL0j8I1Z',
        name: '东风破',
        artist: '周杰伦',
        album: '叶惠美',
        cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000002J4UUk29y8BY_1.jpg',
        duration: 280
      },
      {
        id: '005n9XgF3c9XZ5',
        name: '你听得到',
        artist: '周杰伦',
        album: '叶惠美',
        cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000002J4UUk29y8BY_1.jpg',
        duration: 220
      },
      {
        id: '006m7lKf3aL0Z1',
        name: '同一种调调',
        artist: '周杰伦',
        album: '叶惠美',
        cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000002J4UUk29y8BY_1.jpg',
        duration: 245
      }
    ]
  } catch (error) {
    console.error('获取专辑数据失败:', error)
  }
}

// 播放歌曲
const playSong = (song) => {
  musicStore.playSong(song)
}

// 添加到播放列表
const addToPlaylist = (song) => {
  musicStore.addToPlaylist(song)
}

// 播放全部歌曲
const playAllSongs = () => {
  if (songs.value.length > 0) {
    musicStore.setPlaylist(songs.value, 0)
  }
}

// 格式化歌曲时长
const formatDuration = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
}
</script>

<template>
  <div class="album-page">
    <!-- 专辑头部信息 -->
    <div class="album-header">
      <div class="album-cover">
        <img :src="album.cover" :alt="album.name" />
      </div>
      <div class="album-info">
        <div class="album-type">专辑</div>
        <h1 class="album-name">{{ album.name }}</h1>
        <div class="album-artist">
          <span>歌手：</span>
          <span class="artist-name">{{ album.artist }}</span>
        </div>
        <div class="album-stats">
          <span>发行时间：{{ album.releaseDate }}</span>
          <span>{{ album.songCount }} 首歌曲</span>
        </div>
        <div class="album-description">{{ album.description }}</div>
        <div class="album-actions">
          <button class="action-button play" @click="playAllSongs">
            <img src="@/assets/icons/play.svg" alt="播放" />
            播放全部
          </button>
        </div>
      </div>
    </div>

    <!-- 专辑歌曲列表 -->
    <div class="section">
      <div class="section-header">
        <h2 class="section-title">专辑歌曲</h2>
        <span class="song-count">{{ album.songCount }} 首歌曲</span>
      </div>

      <div class="song-list">
        <div 
          v-for="(song, index) in songs" 
          :key="song.id" 
          class="song-item"
        >
          <div class="song-index">{{ index + 1 }}</div>
          <div class="song-info">
            <h3 class="song-name">{{ song.name }}</h3>
            <p class="song-meta">{{ song.artist }} - {{ song.album }}</p>
          </div>
          <div class="song-duration">{{ formatDuration(song.duration) }}</div>
          <div class="song-actions">
            <button class="action-btn play" @click="playSong(song)">
              <img src="@/assets/icons/play.svg" alt="播放" />
            </button>
            <button class="action-btn add" @click="addToPlaylist(song)">
              <img src="@/assets/icons/plus.svg" alt="添加" />
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.album-page {
  padding: 20px;
  color: #333;
  background-color: #fff;
}

/* 专辑头部信息 */
.album-header {
  display: flex;
  gap: 20px;
  margin-bottom: 40px;
  padding: 20px;
  background-color: #f8f8f8;
  border-radius: 12px;
}

.album-cover {
  width: 200px;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
  background-color: #e8e8e8;
}

.album-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.album-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 12px;
}

.album-type {
  font-size: 12px;
  color: #999;
  padding: 4px 12px;
  background-color: #e8e8e8;
  border-radius: 4px;
  width: fit-content;
}

.album-name {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.album-artist {
  font-size: 14px;
  color: #666;
}

.artist-name {
  color: #1890ff;
  cursor: pointer;
}

.artist-name:hover {
  text-decoration: underline;
}

.album-stats {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #999;
}

.album-description {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.album-actions {
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

.song-count {
  font-size: 14px;
  color: #999;
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

.song-duration {
  font-size: 14px;
  color: #999;
  margin-right: 16px;
  width: 50px;
  text-align: right;
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
</style>