<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../services/api.js'
import { useMusicStore } from '../store/music.js'

const router = useRouter()
const musicStore = useMusicStore()

// 推荐歌单数据
const playlists = ref([])
// 推荐专辑数据
const albums = ref([])
// 加载状态
const loading = ref(false)

// 页面加载时获取推荐数据
onMounted(() => {
  fetchRecommendData()
})

// 获取推荐数据
const fetchRecommendData = async () => {
  try {
    loading.value = true;
    // 获取推荐歌单
    const topListResult = await api.getTopLists()
    if (topListResult) {
      playlists.value = topListResult.data
    }
  } catch (error) {
    console.error('获取推荐数据失败:', error);
  } finally {
    loading.value = false;
  }
}

// 查看歌单详情
const viewPlaylist = (playlistId) => {
  router.push(`/playlist/${playlistId}`)
}

// 查看专辑详情
const viewAlbum = (albumId) => {
  router.push(`/album/${albumId}`)
}

// 播放专辑/歌单
const playAll = (songs, playIndex = 0) => {
  musicStore.setPlaylist(songs, playIndex)
}

// 播放歌曲
const playSong = (song) => {
  musicStore.playSong(song)
}

// 添加到播放列表
const addToPlaylist = (song) => {
  musicStore.addToPlaylist(song)
}
</script>

<template>
  <div class="recommend-page">
    <!-- 页面标题 -->
    <h1 class="page-title">音乐推荐</h1>

    <!-- 推荐歌单 -->
    <div class="section">
      <div class="section-header">
        <h2 class="section-title">推荐歌单</h2>
        <a href="#" class="more-link">更多</a>
      </div>

      <div class="playlist-list">
        <div v-for="playlist in playlists" :key="playlist.id" class="playlist-card" @click="viewPlaylist(playlist.id)">
          <div class="playlist-cover">
            <img :src="playlist.picUrl" :alt="playlist.topTitle" />
            <div class="play-count">
              <img src="@/assets/icons/play.svg" alt="播放" />
              <!-- <span>{{ playlist.playCount > 10000 ? (playlist.playCount / 10000).toFixed(1) + '万' : playlist.playCount }}</span> -->
            </div>
          </div>
          <div class="playlist-info">
            <h3 class="playlist-name">{{ playlist.topTitle }}</h3>
            <p class="playlist-update">{{ playlist.updateFrequency }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 推荐专辑 -->
    <div class="section">
      <div class="section-header">
        <h2 class="section-title">推荐专辑</h2>
        <a href="#" class="more-link">更多</a>
      </div>

      <div class="album-list">
        <div v-for="album in albums" :key="album.id" class="album-card" @click="viewAlbum(album.id)">
          <div class="album-cover">
            <img :src="album.cover" :alt="album.name" />
            <div class="album-overlay">
              <div class="overlay-button play" @click.stop="playSong(album)">
                <img src="@/assets/icons/play.svg" alt="播放" />
              </div>
              <div class="overlay-button add" @click.stop="addToPlaylist(album)">
                <img src="@/assets/icons/plus.svg" alt="添加" />
              </div>
            </div>
          </div>
          <div class="album-info">
            <h3 class="album-name">{{ album.name }}</h3>
            <p class="album-artist">{{ album.artist }}</p>
            <p class="album-date">{{ album.releaseDate }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.recommend-page {
  padding: 20px;
  color: #333;
  background-color: #fff;
}

/* 页面标题 */
.page-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 30px;
  color: #333;
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
}

.more-link {
  color: #666;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.more-link:hover {
  color: #1890ff;
}

/* 歌单列表 */
.playlist-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px;
}

.playlist-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.playlist-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.playlist-cover {
  position: relative;
  width: 100%;
  padding-top: 100%;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 12px;
  background-color: #f5f5f5;
}

.playlist-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.play-count {
  position: absolute;
  bottom: 8px;
  right: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

.play-count img {
  width: 14px;
  height: 14px;
}

.playlist-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.playlist-update {
  font-size: 12px;
  color: #999;
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
  gap: 12px;
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

.album-artist {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.album-date {
  font-size: 12px;
  color: #999;
}
</style>


