<script setup>
import { ref, onMounted, computed } from 'vue';
import { useMusicStore } from '../store/music.js';
import SongList from '../components/SongList.vue';
import { DeleteOutlined } from '@ant-design/icons-vue';

const musicStore = useMusicStore();

// 分页状态
const currentPage = ref(1);
const isEnd = ref(false);

// 获取完整的最近播放歌曲列表并按播放时间排序
const fullSongList = computed(() => {
  return [...musicStore.playlist].sort((a, b) => new Date(b.playTime) - new Date(a.playTime));
});

// 播放全部歌曲
const playAll = () => {
  if (fullSongList.value.length > 0) {
    musicStore.setPlaylist(fullSongList.value, 0);
  }
};

// 播放歌曲
const playSong = song => {
  musicStore.playSong(song);
};

// 从最近播放移除歌曲
const removeSong = song => {
  musicStore.removeFromRecent(song);
};
</script>

<template>
  <div class="playlist-page">
    <div class="playlist-detail">
      <SongList
        :listSongs="fullSongList || []"
        :isEnd="isEnd"
        :currentPage="currentPage"
        :isShowAdd="false"
        :isShowDelete="true"
        @deleteSong="removeSong"
      />
    </div>
  </div>
</template>

<style scoped>
.playlist-page {
  padding: 8px;
  background: #f5f5f5;
}

.playlist-detail {
  height: 100%;
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  overflow: auto;
}
</style>
