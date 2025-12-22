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
import { useI18n } from 'vue-i18n';

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
  return musicStore.playlist.filter(song => song.isFavorite);
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
  const { t } = useI18n();
  message.success(t('favoriteList.cancelFavorite'));
};
</script>

<template>
  <div class="playlist-page main-detail-container">
    <div class="playlist-detail main-detail-content">
      <SongList
        :listSongs="fullSongList || []"
        :isEnd="isEnd"
        :currentPage="currentPage"
        :isShowAdd="false"
        :isShowDelete="false"
      >
        <template #actions="{ item }">
          <div class="control-button" @click="toggleFavorite(item)">
            <component
              :is="item?.isFavorite ? heartFilledIcon : heartOutlineIcon"
              :alt="item?.isFavorite ? t('favoriteList.favorited') : t('favoriteList.favorite')"
            />
          </div>
        </template>
      </SongList>
    </div>
  </div>
</template>

<style scoped>
/* 样式与Playlist.vue保持一致 */
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
  overflow: auto;
}
.control-button {
  width: 32px;
  height: 32px;
  color: #ff4d4f;
  cursor: pointer;
}
</style>

