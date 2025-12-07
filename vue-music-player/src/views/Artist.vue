<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../services/api.js'
import { useMusicStore } from '../store/music.js'

const route = useRoute()
const musicStore = useMusicStore()

// 艺术家信息
const artist = ref({
  id: '',
  name: '',
  avatar: '',
  desc: '',
  musicSize: 0,
  albumSize: 0
})

// 艺术家歌曲数据
const songs = ref([])

// 艺术家专辑数据
const albums = ref([])

// 页面加载时获取艺术家数据
onMounted(() => {
  fetchArtistData()
})

// 获取艺术家数据
const fetchArtistData = async () => {
  try {
    // 模拟艺术家数据
    artist.value = {
      id: '12345',
      name: '周杰伦',
      avatar: 'https://y.gtimg.cn/music/photo_new/T001R300x300M000003rsKF44GyaSk_1.jpg',
      desc: '华语流行男歌手、词曲创作人、演员、MV及电影导演、编剧及制作人。',
      musicSize: 150,
      albumSize: 20
    }

    // 模拟艺术家歌曲数据
    songs.value = [
      {
        id: '001JZkTF2XZ8lH',
        name: '晴天',
        artist: '周杰伦',
        album: '叶惠美',
        cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000002J4UUk29y8BY_1.jpg'
      },
      {
        id: '003NwXtC3aSff4',
        name: '青花瓷',
        artist: '周杰伦',
        album: '我很忙',
        cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000000tJ8Zv2pE96r_1.jpg'
      },
      {
        id: '004XtDOL0j8I1Z',
        name: '七里香',
        artist: '周杰伦',
        album: '七里香',
        cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000000f4aDg0v3gZQ_1.jpg'
      },
      {
        id: '003a6QZJ0wYrB5',
        name: '稻香',
        artist: '周杰伦',
        album: '魔杰座',
        cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000001v65Xk0wU4hI_1.jpg'
      }
    ]

    // 模拟艺术家专辑数据
    albums.value = [
      {
        id: '002J4UUk29y8BY',
        name: '叶惠美',
        artist: '周杰伦',
        cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000002J4UUk29y8BY_1.jpg',
        releaseDate: '2003-07-31'
      },
      {
        id: '000tJ8Zv2pE96r',
        name: '我很忙',
        artist: '周杰伦',
        cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000000tJ8Zv2pE96r_1.jpg',
        releaseDate: '2007-11-02'
      },
      {
        id: '000f4aDg0v3gZQ',
        name: '七里香',
        artist: '周杰伦',
        cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000000f4aDg0v3gZQ_1.jpg',
        releaseDate: '2004-08-03'
      }
    ]
  } catch (error) {
    console.error('获取艺术家数据失败:', error)
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
</script>

<template>
  <div class="artist-page">
    <!-- 艺术家头部信息 -->
    <div class="artist-header">
      <div class="artist-avatar">
        <img :src="artist.avatar" :alt="artist.name" />
      </div>
      <div class="artist-info">
        <h1 class="artist-name">{{ artist.name }}</h1>
        <p class="artist-desc">{{ artist.desc }}</p>
        <div class="artist-stats">
          <span>{{ artist.musicSize }} 首歌曲</span>
          <span>{{ artist.albumSize }} 张专辑</span>
        </div>
        <div class="artist-actions">
          <button class="action-button play" @click="playAllSongs">
            <img src="@/assets/icons/play.svg" alt="播放" />
            播放全部
          </button>
        </div>
      </div>
    </div>

    <!-- 艺术家歌曲列表 -->
    <div class="section">
      <div class="section-header">
        <h2 class="section-title">热门歌曲</h2>
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

    <!-- 艺术家专辑列表 -->
    <div class="section">
      <div class="section-header">
        <h2 class="section-title">专辑作品</h2>
      </div>

      <div class="album-list">
        <div 
          v-for="album in albums" 
          :key="album.id" 
          class="album-card"
        >
          <div class="album-cover">
            <img :src="album.cover" :alt="album.name" />
            <div class="album-overlay">
              <div class="overlay-button play" @click="playSong(album)">
                <img src="@/assets/icons/play.svg" alt="播放" />
              </div>
            </div>
          </div>
          <div class="album-info">
            <h3 class="album-name">{{ album.name }}</h3>
            <p class="album-date">{{ album.releaseDate }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.artist-page {
  padding: 20px;
  color: #333;
  background-color: #fff;
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
</style>