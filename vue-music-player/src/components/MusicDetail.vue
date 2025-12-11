<script setup>
import { ref } from 'vue';

// 音乐详情是否显示
const isShown = ref(true);
// 当前播放歌曲信息
const currentSong = ref({
  name: '晴天',
  artist: '周杰伦',
  album: '叶惠美',
  cover: '@/assets/imgs/album-cover.jpg',
  playCount: '100,000,000+',
  releaseDate: '2003-07-31',
});

// 歌词
const lyrics = ref([
  { time: '00:00', text: '晴天 - 周杰伦' },
  { time: '00:05', text: '作词：周杰伦 作曲：周杰伦' },
  { time: '00:10', text: '故事的小黄花' },
  { time: '00:15', text: '从出生那年就飘着' },
  { time: '00:20', text: '童年的荡秋千' },
  { time: '00:25', text: '随记忆一直晃到现在' },
  { time: '00:30', text: 'Re So So Si Do Si La' },
  { time: '00:35', text: 'So La Si Si Si Si La Si La So' },
  { time: '00:40', text: '吹着前奏望着天空' },
  { time: '00:45', text: '我想起花瓣试着掉落' },
]);

// 隐藏音乐详情
const hideDetail = () => {
  isShown.value = false;
};
</script>

<template>
  <div class="music-detail-container" :class="{ shown: isShown }">
    <!-- 关闭按钮 -->
    <div class="close-button" @click="hideDetail">
      <img src="@/assets/icons/x-mark.svg" alt="关闭" />
    </div>

    <!-- 专辑封面 -->
    <div class="album-cover">
      <img :src="currentSong.cover" :alt="currentSong.name" />
    </div>

    <!-- 歌曲信息 -->
    <div class="song-info">
      <div class="song-name">{{ currentSong.name }}</div>
      <div class="song-artist">{{ currentSong.artist }}</div>
      <div class="song-album">{{ currentSong.album }}</div>
      <div class="song-stats">
        <span>{{ currentSong.playCount }} 播放</span>
        <span>{{ currentSong.releaseDate }}</span>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <div class="action-button primary">
          <img src="@/assets/icons/play.svg" alt="播放" />
          播放
        </div>
        <div class="action-button secondary">
          <img src="@/assets/icons/heart-outline.svg" alt="喜欢" />
        </div>
        <div class="action-button secondary">
          <img src="@/assets/icons/list-bullet.svg" alt="添加到播放列表" />
        </div>
        <div class="action-button secondary">
          <img src="@/assets/icons/array-download-tray.svg" alt="下载" />
        </div>
        <div class="action-button secondary">
          <img src="@/assets/icons/chat-bubble-left-ellipsis.svg" alt="分享" />
        </div>
      </div>
    </div>

    <!-- 歌词 -->
    <div class="lyrics-section">
      <div class="section-title">歌词</div>
      <div class="lyrics-container">
        <div
          v-for="(line, index) in lyrics"
          :key="index"
          class="lyric-line"
          :class="{ active: index === 5 }"
        >
          <div class="lyric-time">{{ line.time }}</div>
          <div class="lyric-text">{{ line.text }}</div>
        </div>
      </div>
    </div>

    <!-- 相关推荐 -->
    <div class="related-section">
      <div class="section-title">相关推荐</div>
      <div class="related-list">
        <div class="related-item">
          <div class="related-cover">
            <img :src="currentSong.cover" alt="相关歌曲" />
          </div>
          <div class="related-info">
            <div class="related-name">晴天 (Live)</div>
            <div class="related-artist">周杰伦</div>
          </div>
        </div>
        <div class="related-item">
          <div class="related-cover">
            <img :src="currentSong.cover" alt="相关歌曲" />
          </div>
          <div class="related-info">
            <div class="related-name">七里香</div>
            <div class="related-artist">周杰伦</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.music-detail-container {
  position: fixed;
  bottom: 80px;
  right: 0;
  width: 350px;
  height: calc(100vh - 80px);
  background-color: #fff;
  border-left: 1px solid #ccc;
  transform: translateX(100%);
  transition: transform 0.3s ease;
  overflow-y: auto;
  z-index: 1000;
}

.music-detail-container.shown {
  transform: translateX(0);
}

.close-button {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.3s;
  z-index: 10;
}

.close-button:hover {
  background-color: #333;
}

.close-button img {
  width: 20px;
  height: 20px;
  opacity: 0.7;
}

.album-cover {
  width: 100%;
  height: 350px;
  overflow: hidden;
}

.album-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.song-info {
  padding: 20px;
}

.song-name {
  font-size: 24px;
  font-weight: bold;
  color: #fff;
  margin-bottom: 8px;
}

.song-artist {
  font-size: 16px;
  color: #ddd;
  margin-bottom: 4px;
}

.song-album {
  font-size: 14px;
  color: #888;
  margin-bottom: 16px;
}

.song-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #888;
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
}

.action-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-button.primary {
  background-color: #1890ff;
  color: #fff;
  font-weight: 500;
}

.action-button.primary:hover {
  background-color: #40a9ff;
}

.action-button.secondary {
  background-color: #333;
  color: #ddd;
  width: 40px;
  height: 40px;
  padding: 0;
  border-radius: 50%;
}

.action-button.secondary:hover {
  background-color: #444;
}

.action-button img {
  width: 20px;
  height: 20px;
  opacity: 0.8;
}

.action-button.primary img {
  opacity: 1;
}

.lyrics-section,
.related-section {
  padding: 20px;
  border-top: 1px solid #333;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #fff;
  margin-bottom: 16px;
}

.lyrics-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.lyric-line {
  display: flex;
  gap: 12px;
  padding: 4px 0;
  font-size: 14px;
  color: #888;
  transition: all 0.3s;
}

.lyric-line.active {
  color: #1890ff;
  transform: translateX(8px);
}

.lyric-time {
  width: 40px;
  font-size: 12px;
  color: #666;
}

.related-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.related-item {
  display: flex;
  gap: 12px;
  cursor: pointer;
  transition: background-color 0.3s;
  padding: 8px;
  border-radius: 6px;
}

.related-item:hover {
  background-color: #333;
}

.related-cover {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  overflow: hidden;
}

.related-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.related-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
}

.related-name {
  font-size: 14px;
  color: #ddd;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.related-artist {
  font-size: 12px;
  color: #888;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
