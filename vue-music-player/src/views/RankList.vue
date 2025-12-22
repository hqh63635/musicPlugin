<script setup>
import { ref, onMounted, computed } from 'vue';
import api from '../services/api.js';
import { useMusicStore } from '../store/music.js';
import playIcon from '@/assets/icons/play.svg?url';
import plusIcon from '@/assets/icons/plus.svg?url';
import { PlayCircleOutlined, PlusCircleOutlined } from '@ant-design/icons-vue';
import SongList from '../components/SongList.vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const musicStore = useMusicStore();

// 排行榜列表
const topLists = ref([]);
// 当前选中的排行榜
const selectedList = ref(null);
// 排行榜歌曲
const listSongs = ref([]);
// 加载状态
const loading = ref(false);
const currentPage = ref(1);
const isEnd = ref(false);

// 页面加载时获取排行榜数据
onMounted(() => {
  fetchTopLists();
});
// 切换排行榜
const selectList = list => {
  selectedList.value = list;
  fetchListDetail(list);
  listSongs.value = list.song || [];
};
// 获取排行榜列表
const fetchTopLists = async () => {
  try {
    loading.value = true;
    const result = await api.getTopLists();

    if (result) {
      topLists.value = result;
      selectList(result?.[0].data?.[0]);
    }
  } catch (error) {
    console.error('获取排行榜列表失败:', error);
  } finally {
    loading.value = false;
  }
};

// 获取排行榜详情
const fetchListDetail = async list => {
  try {
    loading.value = true;
    // 调用专门的排行榜详情API，传入完整的list对象
    const result = await api.getTopListDetail(list);
    if (result) {
      selectedList.value = result;
      listSongs.value = result.musicList;
    }
  } catch (error) {
    console.error('获取排行榜详情失败:', error);
  } finally {
    loading.value = false;
  }
};

// 播放歌曲
const playSong = (song, index) => {
  musicStore.playSong(song);
};

// 播放全部歌曲
const playAllSongs = () => {
  if (listSongs.value.length > 0) {
    musicStore.setPlaylist(listSongs.value, 0);
  }
};

// 添加到播放列表
const addToPlaylist = song => {
  musicStore.addToPlaylist(song);
};

// 获取排名样式
const getRankClass = index => {
  if (index < 3) {
    return `rank-top rank-top-${index + 1}`;
  }
  return 'rank-normal';
};
// 添加格式化数字的工具函数
const formatNumber = value => {
  if (!value) return '0';
  if (value >= 100000000) {
    return (value / 100000000).toFixed(1) + t('rankList.hundredMillion');
  } else if (value >= 10000) {
    return (value / 10000).toFixed(1) + t('rankList.tenThousand');
  }
  return value.toString();
};
</script>

<template>
  <div class="ranklist-page main-detail-container">
    <!-- 排行榜容器 -->
    <div class="ranklist-container main-detail-content">
      <!-- 排行榜列表 -->
      <div class="ranklist-sidebar">
        <div v-for="group in topLists" :key="group.groupName" class="ranklist-group">
          <h2 class="group-title">{{ group.title }}</h2>
          <div
            v-for="list in group.data"
            :key="list.title"
            class="ranklist-item"
            :class="{ active: selectedList?.title === list.title }"
            @click="selectList(list)"
          >
            <div class="ranklist-cover">
              <img
                :src="list.coverImg || '@/assets/default-cover.jpg'"
                :alt="list.title"
                style="width: 40px; height: 40px; object-fit: cover"
              />
            </div>
            <div class="ranklist-info">
              <h3 class="ranklist-name">{{ list.title }}</h3>
              <p class="ranklist-update">
                {{ list.period }} {{ $t('rankList.updated') }} · {{ formatNumber(list.listenNum) }}
                {{ t('common.play') }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- 排行榜详情 -->
      <div class="ranklist-detail" v-if="selectedList">
        <!-- 排行榜头部 -->
        <div class="detail-header">
          <div class="header-cover">
            <img :src="selectedList.coverImg" :alt="selectedList.name" />
            <div class="play-button" @click="playAllSongs">
              <PlayCircleOutlined style="font-size: 20px; margin-right: 8px" />
              <span>{{ $t('rankList.playAll') }}</span>
            </div>
          </div>
          <div class="header-info">
            <h3 class="info-title">{{ selectedList.title }}</h3>
            <!-- <div class="info-stats">
              <span class="stats-count">
                <img :src="plusIcon" :alt="$t('common.songCount')" />
                {{ selectedList?.musicList?.length }}{{ $t('common.songUnit') }}
              </span>
              <span class="stats-play">
                <img :src="playIcon" :alt="$t('common.playCount')" />
                {{
                  selectedList.playCount > 10000
                    ? (selectedList.playCount / 10000).toFixed(1) + $t('rankList.tenThousand')
                    : selectedList.playCount
                }}{{ $t('common.plays') }}
              </span>
            </div> -->
            <p class="info-description" v-html="selectedList.description"></p>
          </div>
        </div>

        <!-- 歌曲列表 -->
        <SongList
          :listSongs="listSongs"
          :currentPage="currentPage"
          :isShowAdd="true"
          :isShowDelete="false"
        >
        </SongList>
      </div>

      <!-- 未选择排行榜提示 -->
      <div class="no-selection" v-else>
        <img src="@/assets/icons/musical-note.svg" :alt="$t('rankList.selectRanking')" />
        <p>{{ $t('rankList.selectToView') }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 排行榜容器 */
.ranklist-container {
  display: flex;
  height: 100%;
  box-shadow: 0 2px 10px var(--theme-shadow-primary);
}

/* 排行榜侧边栏 */
.ranklist-sidebar {
  width: 300px;
  border-radius: 12px 0 0 12px;
  overflow-y: auto;
  background-color: var(--theme-bg-primary);
  border-right: 1px solid var(--theme-border-primary);
  transition:
    background-color 0.3s,
    border-right-color 0.3s;
}

.ranklist-group {
  padding: 8px;
}

.group-title {
  font-size: 16px;
  margin-bottom: 15px;
  color: var(--theme-text-secondary);
  font-weight: 600;
  padding-left: 10px;
  border-left: 3px solid var(--theme-accent-primary);
  transition:
    color 0.3s,
    border-left-color 0.3s;
}

.ranklist-item {
  display: flex;
  align-items: center;
  padding: 12px;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 8px;
  margin-bottom: 8px;
}

.ranklist-item:hover {
  background-color: var(--theme-bg-hover);
}

.ranklist-item.active {
  background-color: var(--theme-bg-active);
  border-left: 3px solid var(--theme-accent-primary);
}
.ranklist-item.active .ranklist-name {
  color: var(--theme-accent-primary);
}
.ranklist-cover {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
}

.ranklist-info {
  flex: 1;
  min-width: 0;
}

.ranklist-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--theme-text-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.3s;
}

.ranklist-update {
  font-size: 12px;
  color: var(--theme-text-tertiary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.3s;
}

/* 歌曲列表优化 */
.song-item {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 80px;
  padding: 12px 15px;
  border-radius: 8px;
  transition: all 0.2s;
  margin-bottom: 8px;
  width: 100%;
  box-sizing: border-box;
}

.song-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 15px;
  min-width: 0;
  padding-right: 15px;
}

.song-duration {
  color: var(--theme-text-tertiary);
  font-size: 14px;
  padding-left: 15px;
  white-space: nowrap;
  flex-shrink: 0;
  transition: color 0.3s;
  min-width: 50px;
}

.song-rank {
  text-align: center;
  font-size: 16px;
  font-weight: 500;
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
  color: var(--theme-text-secondary);
  transition: color 0.3s;
}

.song-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 15px;
  min-width: 0;
}

.song-cover {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
}

.song-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.song-details {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.song-name {
  font-size: 16px;
  color: var(--theme-text-primary);
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
  transition: color 0.3s;
}

.song-artist {
  font-size: 13px;
  color: var(--theme-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.3s;
}

.meta-separator {
  color: var(--theme-border-primary);
  transition: color 0.3s;
}

.song-album {
  font-size: 13px;
  color: var(--theme-text-tertiary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.3s;
}

.song-duration {
  color: var(--theme-text-tertiary);
  font-size: 14px;
  padding-left: 15px;
  white-space: nowrap;
  flex-shrink: 0;
  transition: color 0.3s;
}

/* 排行榜详情头部优化 */
.detail-header {
  height: 120px;
  display: flex;
  gap: 20px;
  padding-bottom: 8px;
}

.header-cover {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 15px var(--theme-shadow-primary);
  transition: box-shadow 0.3s;
}

.header-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.header-info {
  flex: 1;
  min-width: 0;
}

.info-title {
  font-size: 20px;
  line-height: 20px;
  font-weight: bold;
  color: var(--theme-text-primary);
  margin-bottom: 8px;
}

.info-stats {
  display: flex;
  gap: 25px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.stats-count,
.stats-play {
  display: flex;
  align-items: center;
  color: #666;
  font-size: 14px;
}

.stats-count img,
.stats-play img {
  width: 18px;
  height: 18px;
  margin-right: 8px;
}

.info-description {
  color: var(--theme-text-secondary);
  line-height: 1.2;
  font-size: 12px;
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
.song-header {
  text-align: center;
  height: 40px;
  line-height: 40px;
  margin-top: 12px;
  font-size: 16px;
  font-weight: 500;
  color: #666;
  background-color: #f9f9f9;
}
.song-list {
  flex: 1;
  overflow: auto;
}
.ranklist-detail {
  flex: 1;
  min-width: 0;
  padding: 8px;
  display: flex;
  flex-direction: column;
}
.list-song-info {
  display: flex;
  flex: 1;
  min-width: 0;
}
.list-song-info-title {
  flex: 1;
  min-width: 0;
  font-size: 16px;
  font-weight: 500;
  margin-left: 20px;
  align-self: center;
  justify-items: center;
  display: flex;
}
</style>
