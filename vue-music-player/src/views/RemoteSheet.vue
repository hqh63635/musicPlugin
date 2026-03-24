<template>
  <div class="remote-sheet">
    <div v-if="state.loading" class="loading">{{ t('remoteSheet.loading') }}</div>
    <div v-else-if="!sheetItem" class="empty">{{ t('remoteSheet.loadFailed') }}</div>
    <div v-else>
      <div class="sheet-header">
        <div class="sheet-cover">
          <img :src="sheetItem.cover || ''" :alt="t('remoteSheet.coverAlt')" />
        </div>
        <div class="sheet-info">
          <h1 class="sheet-title">{{ sheetItem.title }}</h1>
          <p class="sheet-creator">{{ $t('remoteSheet.creator') }}: {{ sheetItem.creator || $t('remoteSheet.unknown') }}</p>
          <p class="sheet-desc">{{ sheetItem.description || $t('remoteSheet.noDescription') }}</p>
          <div class="sheet-stats">
            <span>{{ sheetItem.playCount || 0 }} {{ $t('remoteSheet.plays') }}</span>
            <span>{{ sheetItem.trackCount || 0 }} {{ $t('remoteSheet.songs') }}</span>
          </div>
          <div class="sheet-actions">
            <button @click="toggleStar" class="star-button">
              <svg v-if="isStarred" class="icon" viewBox="0 0 24 24" fill="currentColor">
                <path
                  d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
                />
              </svg>
              <svg v-else class="icon" viewBox="0 0 24 24" fill="currentColor">
                <path
                  d="M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z"
                />
              </svg>
              {{ isStarred ? t('remoteSheet.unstar') : t('remoteSheet.star') }}
            </button>
          </div>
        </div>
      </div>
      <div class="song-list-container">
        <songList :musicList="musicList" />
        <div v-if="state.loadingMore" class="loading-more">{{ $t('remoteSheet.loadingMore') }}</div>
        <div v-if="state.isEnd" class="no-more">{{ $t('remoteSheet.noMore') }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useI18n } from 'vue-i18n';
import api from '../services/api.js';
import songList from '../components/songList.vue';
import { useMusicStore } from '../store/music.js';

const { t } = useI18n();

// 获取路由参数
const route = useRoute();
const store = useMusicStore();

// 响应式状态
const state = reactive({
  loading: true,
  loadingMore: false,
  isEnd: false,
});

const sheetItem = ref(null);
const musicList = ref([]);
const currentPage = ref(1);

// 计算是否已收藏
const isStarred = computed(() => {
  if (!sheetItem.value) return false;
  return store.isSheetFavorite(sheetItem.value.platform, sheetItem.value.id);
});

// 获取歌单详情
const getSheetDetail = async () => {
  if ((state.loading && currentPage.value === 1) || state.loadingMore) return;

  const { platform, id } = route.params;
  if (!platform || !id) return;

  // 设置加载状态
  if (currentPage.value === 1) {
    state.loading = true;
  } else {
    state.loadingMore = true;
  }

  try {
    const result = await api.getMusicSheetInfo(platform, id, currentPage.value);

    // 更新歌单信息
    if (result.sheetItem && currentPage.value === 1) {
      sheetItem.value = {
        ...sheetItem.value,
        ...result.sheetItem,
        platform,
        id,
      };
    }

    // 更新歌曲列表
    if (result.musicList) {
      if (currentPage.value === 1) {
        musicList.value = result.musicList;
      } else {
        musicList.value = [...musicList.value, ...result.musicList];
      }
    }

    // 更新分页状态
    state.isEnd = result.isEnd || false;
    currentPage.value += 1;
  } catch (error) {
    console.error(t('remoteSheet.fetchError'), error);
  } finally {
    // 重置加载状态
    state.loading = false;
    state.loadingMore = false;
  }
};

// 收藏/取消收藏歌单
const toggleStar = () => {
  if (!sheetItem.value) return;
  store.toggleFavoriteSheet(sheetItem.value);
};

// 监听路由参数变化
watch(
  () => [route.params.platform, route.params.id],
  () => {
    // 重置状态
    currentPage.value = 1;
    sheetItem.value = null;
    musicList.value = [];
    state.loading = true;
    state.isEnd = false;
    // 重新获取数据
    getSheetDetail();
  },
  { immediate: true }
);

// 监听滚动事件，实现无限加载
const handleScroll = () => {
  const container = document.querySelector('.song-list-container');
  if (!container || state.loadingMore || state.isEnd) return;

  const { scrollTop, clientHeight, scrollHeight } = container;
  if (scrollTop + clientHeight >= scrollHeight - 100) {
    getSheetDetail();
  }
};

onMounted(() => {
  const container = document.querySelector('.song-list-container');
  if (container) {
    container.addEventListener('scroll', handleScroll);
  }
});
</script>

<style scoped>
.remote-sheet {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.loading,
.empty {
  text-align: center;
  padding: 50px 0;
  font-size: 16px;
  color: #666;
}

.sheet-header {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f5f5f5;
  border-radius: 8px;
}

.sheet-cover {
  width: 200px;
  height: 200px;
  flex-shrink: 0;
}

.sheet-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.sheet-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.sheet-title {
  margin: 0 0 10px 0;
  font-size: 28px;
  color: #333;
}

.sheet-creator,
.sheet-desc,
.sheet-stats {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 14px;
}

.sheet-desc {
  line-height: 1.5;
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.sheet-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.sheet-actions {
  display: flex;
  gap: 10px;
}

.star-button {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.star-button:hover {
  background-color: #40a9ff;
}

.icon {
  width: 16px;
  height: 16px;
}

.song-list-container {
  max-height: 600px;
  overflow-y: auto;
}

.loading-more,
.no-more {
  text-align: center;
  padding: 20px;
  color: #666;
  font-size: 14px;
}
</style>



