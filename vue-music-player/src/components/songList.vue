<template>
  <!-- 歌曲列表 -->
  <div class="song-list-container">
    <div class="song-list">
      <a-row class="song-header">
        <a-col :span="2" class="">序号
        </a-col>
        <a-col :span="12" class="">
          歌曲
        </a-col>
        <a-col :span="6" class="">歌手
        </a-col>
        <a-col :span="4" class="">
          时长
        </a-col>
      </a-row>
      <a-row v-for="(song, index) in listSongs" :key="index" class="song-item">
        <a-col :span="2" class="song-rank">{{index + 1}}</a-col>
        <a-col :span="12">
          <div class="list-song-info">
            <div class="song-cover" style="width: 60px; height: 60px;">
              <img :src="song.artwork?.replace(/[`\s]/g, '') || '@/assets/default-cover.jpg'" :alt="song.title" width="60px" height="60px" />
            </div>
            <div class="list-song-info-title" style="flex: 1; min-width: 0;">
              <div style="flex: 1; min-width: 0;">
                <span>{{ song.title }}</span>
                <!-- <span> {{ song.subtitle }}</span> -->
              </div>
              <span class="song-actions">
                <PlayCircleOutlined font-size="16px" @click="playSong(song, index)" />
                <!-- <img :src="playIcon" alt="播放" @click="playSong(song, index)" />
                  <img :src="plusIcon" alt="添加" @click="addToPlaylist(song)" /> -->
              </span>
            </div>

          </div>

        </a-col>
        <a-col :span="6" class="song-rank">
          {{song.artist}}
        </a-col>
        <a-col :span="4" class="song-rank"></a-col>
      </a-row>
    </div>
    <!-- 分页组件 - 使用统一的组件名称 -->
    <div class="pagination-container">
      <a-pagination v-model:current="currentPage" :page-size="pageSize" :total="total" @change="handlePageChange" show-size-changer show-quick-jumper :show-total="(total) => `共 ${total} 条记录`" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useMusicStore } from '../store/music.js';
import playIcon from '@/assets/icons/play.svg?url';
import plusIcon from '@/assets/icons/plus.svg?url';
import { PlayCircleOutlined } from '@ant-design/icons-vue';
// 移除循环导入
// import SongList from '../components/songList.vue';

// 正确导入分页组件
// 恢复标准导入方式
// 恢复分页组件导入
// 使用全局注册的组件，无需本地导入
// import { Pagination } from 'ant-design-vue';

const musicStore = useMusicStore();
const props = defineProps({
  listSongs: {
    type: Array,
    default: () => [],
  },
  pageSize: {
    type: Number,
    default: 20,
  },
  total: {
    type: Number,
    default: 0,
  },
});
const currentPage = ref(1);
// 播放歌曲
const playSong = (song, index) => {
  musicStore.playSong(song);
};
const emit = defineEmits(['pageChange']);

const handlePageChange = (page, pageSize) => {
  emit('pageChange', page, pageSize);
};
</script>
<style scoped>
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
.song-rank {
  text-align: center;
  font-size: 16px;
  font-weight: 500;
}
.song-header {
  text-align: center;
  height: 40px;
  line-height: 40px;
  font-size: 16px;
  font-weight: 500;
  color: #666;
  background-color: #f9f9f9;
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
.song-item:hover {
  background-color: #f0f7ff;
  cursor: pointer;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
  padding: 10px;
}
</style>