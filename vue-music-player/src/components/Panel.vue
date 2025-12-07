<script setup>
import { ref } from 'vue'
import { useMusicStore } from '../store/music.js'

// 导入SVG图标
import UserIcon from '@/assets/icons/user.svg'

const musicStore = useMusicStore()

// 热门歌曲数据
const hotSongs = ref([])

// 加载模拟数据
const loadMockData = () => {
  hotSongs.value = [
    {
      id: '001JZkTF2XZ8lH',
      name: '晴天',
      artist: '周杰伦',
      album: '叶惠美',
      cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000002J4UUk29y8BY_1.jpg',
      playCount: '100,000,000+'
    },
    {
      id: '003a6QZJ0wYrB5',
      name: '稻香',
      artist: '周杰伦',
      album: '魔杰座',
      cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000001v65Xk0wU4hI_1.jpg',
      playCount: '80,000,000+'
    },
    {
      id: '004XtDOL0j8I1Z',
      name: '七里香',
      artist: '周杰伦',
      album: '七里香',
      cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000000f4aDg0v3gZQ_1.jpg',
      playCount: '90,000,000+'
    }
  ]
}

// 初始化数据
loadMockData()

// 播放歌曲
const playSong = (song) => {
  musicStore.playSong(song)
}
</script>

<template>
  <div class="panel-container">
    <!-- 热门歌曲 -->
    <div class="hot-songs-section">
      <div class="section-header">
        <span>热门歌曲</span>
      </div>
      <div class="hot-songs-list">
        <div 
          v-for="(song, index) in hotSongs" 
          :key="song.id" 
          class="hot-song-item"
          @click="playSong(song)"
        >
          <div class="song-rank">{{ index + 1 }}</div>
          <div class="song-cover">
            <img :src="song.cover" :alt="song.name" />
          </div>
          <div class="song-info">
            <div class="song-name">{{ song.name }}</div>
            <div class="song-artist">{{ song.artist }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 个人中心入口 -->
    <div class="user-center-section">
      <div class="user-center-card">
        <div class="user-icon">
          <UserIcon alt="用户中心" />
        </div>
        <div class="user-center-info">
          <div class="user-center-title">个人中心</div>
          <div class="user-center-desc">查看你的音乐收藏和历史记录</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.panel-container {
  width: 280px;
  height: 100%;
  background-color: #ffffff;
  border-left: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  color: #333;
}

/* 热门歌曲区域 */
.hot-songs-section {
  padding: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 500;
}

.hot-songs-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hot-song-item {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: background-color 0.3s;
  padding: 8px;
  border-radius: 8px;
}

.hot-song-item:hover {
  background-color: #f5f5f5;
}

.song-rank {
  width: 24px;
  font-size: 14px;
  color: #999;
  text-align: center;
}

.song-cover {
  width: 48px;
  height: 48px;
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
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.song-artist {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 个人中心区域 */
.user-center-section {
  padding: 0 20px 20px;
}

.user-center-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background-color: #f0f0f0;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.user-center-card:hover {
  background-color: #e0e0e0;
}

.user-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: #e0e0e0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.user-icon svg {
  width: 24px;
  height: 24px;
}

.user-center-info {
  flex: 1;
}

.user-center-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.user-center-desc {
  font-size: 12px;
  color: #999;
}
</style>