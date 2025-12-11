<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import api from '../services/api.js';
import { useMusicStore } from '../store/music.js';
import SongList from '../components/SongList.vue';
import { DeleteOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import heartOutlineIcon from '@/assets/icons/heart-outline.svg';
import heartFilledIcon from '@/assets/icons/heart.svg';

const route = useRoute();
const musicStore = useMusicStore();

// 加载状态
const loading = ref(false);
const isEnd = ref(false);
const currentPage = ref(1);
// 每页显示的歌曲数量
const pageSize = ref(20);

// 获取完整的最近播放歌曲列表并按播放时间排序
const fullSongList = computed(() => {
  return musicStore.playlist
    .filter(song => song.isFavorite)
    .sort((a, b) => new Date(b.playTime) - new Date(a.playTime));
});

// 计算当前页的歌曲列表（本地分页）
const songList = computed(() => {
  const startIndex = (currentPage.value - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;
  return fullSongList.value.slice(0, endIndex);
});

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(fullSongList.value.length / pageSize.value);
});

// 监听当前页变化，确保不超过总页数
const handlePageChange = page => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
    isEnd.value = page >= totalPages.value;
  }
};

// 初始化时检查是否为最后一页
onMounted(() => {
  handlePageChange(1);
  // 这里应该有获取最近播放列表的API调用
  // musicStore.getRecentPlaylist();
});

// 播放全部歌曲
const playAll = () => {
  if (fullSongList.value.length > 0) {
    musicStore.setPlaylist(fullSongList.value, 0);
  }
};

// 播放歌曲
const playSong = (song, index) => {
  musicStore.playSong(song);
};

// 添加到播放列表
const addToPlaylist = song => {
  musicStore.addToPlaylist(song);
};

// 切换收藏状态
const toggleFavorite = (song, forceState) => {
  const index = musicStore.playlist.findIndex(item => item.id === song.id);
  if (index !== -1) {
    // 如果提供了forceState参数，则直接设置，否则切换状态
    const newState = forceState !== undefined ? forceState : !musicStore.playlist[index].isFavorite;
    musicStore.playlist[index].isFavorite = newState;

    // 如果是当前播放的歌曲，同步更新currentSong的状态
    if (musicStore.currentSong && musicStore.currentSong.id === song.id) {
      musicStore.currentSong.isFavorite = newState;
    }
  }
};

// 从收藏列表移除歌曲（取消收藏）
const removeSong = song => {
  toggleFavorite(song, false);
  message.success('取消收藏');
};
</script>

<template>
  <div class="playlist-page">
    <div class="playlist-detail">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading">加载中...</div>
      <SongList
        :listSongs="songList || []"
        :isEnd="isEnd"
        :currentPage="currentPage"
        @pageChange="handlePageChange"
        :isShowAdd="false"
        :isShowDelete="false"
        ><div class="control-button" @click="toggleFavorite">
          <component
            :is="musicStore.currentSong?.isFavorite ? heartFilledIcon : heartOutlineIcon"
            :alt="musicStore.currentSong?.isFavorite ? '已喜欢' : '喜欢'"
          />
        </div>
      </SongList>
    </div>
  </div>
</template>

<style scoped>
/* 样式与Playlist.vue保持一致 */
.playlist-page {
  padding: 12px;
  background: #f5f5f5;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
  font-size: 18px;
  color: #888;
}

.playlist-detail {
  height: 100%;
  background: #fff;
  border-radius: 4px;
  padding: 12px;
  overflow: auto;
}
</style>
