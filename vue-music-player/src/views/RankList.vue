<script setup>
import { ref, onMounted, computed } from 'vue';
import api from '../services/api.js';
import { useMusicStore } from '../store/music.js';
import playIcon from '@/assets/icons/play.svg?url';
import plusIcon from '@/assets/icons/plus.svg?url';
import { PlayCircleOutlined, PlusCircleOutlined } from '@ant-design/icons-vue';
import SongList from '../components/SongList.vue';

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

// 获取排行榜列表
const fetchTopLists = async () => {
  try {
    loading.value = true;
    const result = await api.getTopLists();

    if (result) {
      topLists.value = result;
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

// 切换排行榜
const selectList = list => {
  selectedList.value = list;
  fetchListDetail(list);
  listSongs.value = list.song || [];
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
    return (value / 100000000).toFixed(1) + '亿';
  } else if (value >= 10000) {
    return (value / 10000).toFixed(1) + '万';
  }
  return value.toString();
};
</script>

<template>
  <div class="ranklist-page">
    <!-- 排行榜容器 -->
    <div class="ranklist-container">
      <!-- 排行榜列表 -->
      <div class="ranklist-sidebar">
        <div v-for="group in topLists" :key="group.groupName" class="ranklist-group">
          <h2 class="group-title">{{ group.title }}</h2>
          <div v-for="list in group.data" :key="list.title" class="ranklist-item" :class="{ active: selectedList?.title === list.title }" @click="selectList(list)">
            <div class="ranklist-cover">
              <img :src="list.coverImg || '@/assets/default-cover.jpg'" :alt="list.title" style="width: 40px; height: 40px; object-fit: cover;" />
            </div>
            <div class="ranklist-info">
              <h3 class="ranklist-name">{{ list.title }}</h3>
              <p class="ranklist-update">{{ list.period }} 更新 · {{ formatNumber(list.listenNum) }} 播放</p>
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
              <PlayOutlined style="font-size: 20px; margin-right: 8px;" />
              <span>播放全部</span>
            </div>
          </div>
          <div class="header-info">
            <h3 class="info-title">{{ selectedList.title }}</h3>
            <!-- <div class="info-stats">
              <span class="stats-count">
                <img src="@/assets/icons/list-bullet.svg" alt="歌曲数" />
                {{ selectedList?.musicList?.length }}首 
              </span>
              <span class="stats-play">
                <img src="@/assets/icons/play.svg" alt="播放量" />
                {{ selectedList.playCount > 10000 ? (selectedList.playCount / 10000).toFixed(1) + '万' : selectedList.playCount }}次播放
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
        <img src="@/assets/icons/musical-note.svg" alt="选择排行榜" />
        <p>请选择一个排行榜查看详情</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ranklist-page {
  padding: 12px;
  color: #333;
  border-radius: 4px;
  background-color: #f5f5f5;
}

/* 排行榜容器 */
.ranklist-container {
  display: flex;
  height: calc(100vh - 162px);
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

/* 排行榜侧边栏 */
.ranklist-sidebar {
  width: 300px;
  overflow-y: auto;
  background-color: #f9f9f9;
  border-right: 1px solid #eee;
}

.ranklist-group {
  padding: 15px;
}

.group-title {
  font-size: 16px;
  margin-bottom: 15px;
  color: #666;
  font-weight: 600;
  padding-left: 10px;
  border-left: 3px solid #1890ff;
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
  background-color: #f0f7ff;
}

.ranklist-item.active {
  background-color: #e6f7ff;
  border-left: 3px solid #1890ff;
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
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ranklist-update {
  font-size: 12px;
  color: #888;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  color: #999;
  font-size: 14px;
  padding-left: 15px;
  white-space: nowrap;
  flex-shrink: 0;
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
  color: #888;
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
  color: #333;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.song-artist {
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta-separator {
  color: #ddd;
}

.song-album {
  font-size: 13px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.song-duration {
  color: #999;
  font-size: 14px;
  padding-left: 15px;
  white-space: nowrap;
  flex-shrink: 0;
}

/* 排行榜详情头部优化 */
.detail-header {
  height: 120px;
  display: flex;
  gap: 20px;
  padding: 12px;
  background-color: #f9f9f9;
  border-radius: 12px;
}

.header-cover {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
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
  color: #333;
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
  color: #666;
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
  padding: 12px;
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





