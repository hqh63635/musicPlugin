<script setup>
import { ref, onMounted, computed } from 'vue'
import api from '../services/api.js'
import { useMusicStore } from '../store/music.js'

const musicStore = useMusicStore()

// 排行榜列表
const topLists = ref([])
// 当前选中的排行榜
const selectedList = ref(null)
// 排行榜歌曲
const listSongs = ref([])
// 加载状态
const loading = ref(false)

// 页面加载时获取排行榜数据
onMounted(() => {
  fetchTopLists()
})

// 获取排行榜列表
const fetchTopLists = async () => {
  try {
    loading.value = true
    const result = await api.getTopLists()
    if (result.success) {
      topLists.value = result.data
    }
  } catch (error) {
    console.error('获取排行榜列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取排行榜详情
const fetchListDetail = async (list) => {
  try {
    loading.value = true
    // 调用专门的排行榜详情API，传入完整的list对象
    const result = await api.getTopListDetail(list)
    if (result.success) {
      selectedList.value = result.data.topListItem
      listSongs.value = result.data.musicList
    }
  } catch (error) {
    console.error('获取排行榜详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 切换排行榜
const selectList = (list) => {
  // 先重置selectedList和listSongs
  selectedList.value = null
  listSongs.value = []
  // 然后获取排行榜详情
  fetchListDetail(list)
}

// 播放歌曲
const playSong = (song, index) => {
  musicStore.playSong(song)
}

// 播放全部歌曲
const playAllSongs = () => {
  if (listSongs.value.length > 0) {
    musicStore.setPlaylist(listSongs.value, 0)
  }
}

// 添加到播放列表
const addToPlaylist = (song) => {
  musicStore.addToPlaylist(song)
}

// 获取排名样式
const getRankClass = (index) => {
  if (index < 3) {
    return `rank-top rank-top-${index + 1}`
  }
  return 'rank-normal'
}
</script>

<template>
  <div class="ranklist-page">
    <!-- 页面标题 -->
    <h1 class="page-title">排行榜</h1>
    
    <!-- 排行榜容器 -->
    <div class="ranklist-container">
      <!-- 排行榜列表 -->
      <div class="ranklist-sidebar">
        <div 
          v-for="list in topLists" 
          :key="list.id"
          class="ranklist-item"
          :class="{ active: selectedList?.id === list.id }"
          @click="selectList(list)"
        >
          <div class="ranklist-cover">
            <img :src="list.picUrl" :alt="list.topTitle" />
          </div>
          <div class="ranklist-info">
            <h3 class="ranklist-name">{{ list.topTitle }}</h3>
            <p class="ranklist-update">{{ list.listenCount }}</p>
          </div>
        </div>
      </div>
      
      <!-- 排行榜详情 -->
      <div class="ranklist-detail" v-if="selectedList">
        <!-- 排行榜头部 -->
        <div class="detail-header">
          <div class="header-cover">
            <img :src="selectedList.cover" :alt="selectedList.name" />
            <div class="play-button" @click="playAllSongs">
              <img src="@/assets/icons/play.svg" alt="播放全部" />
              <span>播放全部</span>
            </div>
          </div>
          <div class="header-info">
            <div class="info-type">排行榜</div>
            <h2 class="info-title">{{ selectedList.name }}</h2>
            <div class="info-stats">
              <span class="stats-count">
                <img src="@/assets/icons/list-bullet.svg" alt="歌曲数" />
                {{ selectedList.songCount }}首
              </span>
              <span class="stats-play">
                <img src="@/assets/icons/play.svg" alt="播放量" />
                {{ selectedList.playCount > 10000 ? (selectedList.playCount / 10000).toFixed(1) + '万' : selectedList.playCount }}次播放
              </span>
            </div>
            <p class="info-description">{{ selectedList.description }}</p>
          </div>
        </div>
        
        <!-- 歌曲列表 -->
        <div class="song-list">
          <div class="song-header">
            <div class="header-rank">#</div>
            <div class="header-title">标题</div>
            <div class="header-duration">时长</div>
          </div>
          
          <div 
            v-for="(song, index) in listSongs" 
            :key="song.id"
            class="song-item"
          >
            <!-- 排名 -->
            <div class="song-rank">
              <span :class="getRankClass(index)">{{ index + 1 }}</span>
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
              <p class="song-artist">
                <span v-for="(artist, i) in song.artist" :key="i">
                  {{ artist }}{{ i < song.artist.length - 1 ? ' / ' : '' }}
                </span>
                <span class="song-album">{{ song.album }}</span>
              </p>
            </div>
            
            <!-- 歌曲时长 -->
            <div class="song-duration">
              {{ musicStore.formatTime(song.duration) }}
            </div>
          </div>
        </div>
      </div>
      
      <!-- 未选择排行榜提示 -->
      <div class="no-selection" v-else>
        <img src="@/assets/icons/musical-note.svg" alt="选择排行榜" />
        <p>请选择一个排行榜查看详情</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ranklist-page {
  padding: 20px;
  color: #ccc;
}

/* 页面标题 */
.page-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 30px;
  color: #ccc;
}

/* 排行榜容器 */
.ranklist-container {
  display: flex;
  gap: 20px;
  height: calc(100vh - 200px);
}

/* 排行榜侧边栏 */
.ranklist-sidebar {
  width: 280px;
  overflow-y: auto;
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
}

.ranklist-item {
  display: flex;
  align-items: center;
  padding: 12px;
  gap: 12px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.ranklist-item:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.ranklist-item.active {
  background-color: rgba(24, 144, 255, 0.2);
  border-left: 3px solid #1890ff;
}

.ranklist-cover {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
}

.ranklist-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.ranklist-info {
  flex: 1;
  min-width: 0;
}

.ranklist-name {
  font-size: 14px;
  font-weight: 500;
  color: #000;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ranklist-update {
  font-size: 12px;
  color: #888;
}

/* 排行榜详情 */
.ranklist-detail {
  flex: 1;
  overflow-y: auto;
}

/* 详情头部 */
.detail-header {
  display: flex;
  gap: 20px;
  padding: 20px;
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  margin-bottom: 20px;
}

.header-cover {
  position: relative;
  width: 160px;
  height: 160px;
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
  bottom: 10px;
  right: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
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
  width: 16px;
  height: 16px;
}

.play-button span {
  font-size: 14px;
  font-weight: 500;
  color: #000;
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

.info-title {
  font-size: 24px;
  font-weight: bold;
  color: #ccc;
}

.info-stats {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #888;
}

.info-stats img {
  width: 16px;
  height: 16px;
  margin-right: 4px;
  vertical-align: text-bottom;
}

.info-description {
  font-size: 14px;
  color: #ccc;
  line-height: 1.5;
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

.header-rank {
  width: 80px;
  text-align: center;
}

.header-title {
  flex: 1;
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

.song-rank {
  position: relative;
  width: 80px;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
}

.rank-top {
  font-weight: bold;
  color: #000;
}

.rank-top-1 {
  color: #ff4d4f;
}

.rank-top-2 {
  color: #fa8c16;
}

.rank-top-3 {
  color: #faad14;
}

.rank-normal {
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

.song-item:hover .song-rank span {
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
  color: #000;
}

.song-artist {
  font-size: 12px;
  color: #888;
}

.song-album {
  margin-left: 10px;
  color: #666;
}

.song-duration {
  width: 80px;
  text-align: right;
  font-size: 12px;
  color: #888;
}

/* 未选择排行榜提示 */
.no-selection {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 20px;
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
}

.no-selection img {
  width: 80px;
  height: 80px;
  opacity: 0.3;
}

.no-selection p {
  font-size: 16px;
  color: #888;
}
</style>
