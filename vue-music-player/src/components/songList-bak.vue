<template>
  <a-row class="song-header">
    <a-col :span="2" class="">序号 </a-col>
    <a-col :span="12" class=""> 歌曲 </a-col>
    <a-col :span="4" class="">歌手 </a-col>
    <a-col :span="4" class=""> 时长 </a-col>
    <a-col :span="2" class=""> 操作 </a-col>
  </a-row>
  <a-list
    class="demo-loadmore-list"
    :loading="initLoading"
    item-layout="horizontal"
    :data-source="listSongs"
  >
    <template #loadMore>
      <div
        v-if="!initLoading && !isEnd"
        :style="{ textAlign: 'center', marginTop: '12px', height: '32px', lineHeight: '32px' }"
      >
        <a-button @click="onLoadMore">加载更多</a-button>
      </div>
    </template>
    <template #renderItem="{ item, index }">
      <a-list-item>
        <template #actions>
          <slot name="actions" :item="item" :index="index"></slot>
          <PlusCircleOutlined v-if="isShowAdd" font-size="16px" @click="addToPlaylist(item)" />
          <DeleteOutlined v-if="isShowDelete" font-size="16px" @click="removeSong(item, index)" />
        </template>
        <!-- 歌曲列表 -->
        <div class="song-list-container">
          <div class="song-list">
            <a-row class="song-item">
              <a-col :span="2" class="song-rank">{{ index + 1 }}</a-col>
              <a-col :span="12">
                <div class="list-song-info">
                  <div class="song-cover" style="width: 60px; height: 60px">
                    <img
                      :src="item.artwork?.replace(/[`\s]/g, '') || '@/assets/default-cover.jpg'"
                      :alt="item.title"
                      width="60px"
                      height="60px"
                    />
                  </div>
                  <div class="list-song-info-title" style="flex: 1; min-width: 0">
                    <div style="flex: 1; min-width: 0">
                      <span>{{ item.title }}</span>
                      <!-- <span> {{ song.subtitle }}</span> -->
                    </div>
                    <span class="song-actions">
                      <PlayCircleOutlined font-size="16px" @click="playSong(item, index)" />
                      <!-- <img :src="playIcon" alt="播放" @click="playSong(song, index)" />
                  <img :src="plusIcon" alt="添加" @click="addToPlaylist(song)" /> -->
                    </span>
                  </div>
                </div>
              </a-col>
              <a-col :span="6" class="song-rank">
                {{ item.artist }}
              </a-col>
              <a-col :span="4" class="song-rank"></a-col>
            </a-row>
          </div>
        </div>
      </a-list-item>
    </template>
  </a-list>
</template>

<script setup>
import { ref } from 'vue';
import { useMusicStore } from '../store/music.js';
import playIcon from '@/assets/icons/play.svg?url';
import plusIcon from '@/assets/icons/plus.svg?url';
import { PlayCircleOutlined, PlusCircleOutlined, DeleteOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
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
  isShowAdd: {
    type: Boolean,
    default: true,
  },
  isShowDelete: {
    type: Boolean,
    default: false,
  },
  isEnd: {
    type: Boolean,
    default: false,
  },
  currentPage: {
    type: Number,
    default: 1,
  },
});
const initLoading = ref(false);
// 播放歌曲
const playSong = (song, index) => {
  musicStore.playSong(song);
};
const emit = defineEmits(['pageChange']);

const handlePageChange = (page, pageSize) => {
  emit('pageChange', page, pageSize);
};
const onLoadMore = () => {
  if (props.isEnd) return;
  const newPage = props.currentPage + 1;
  emit('pageChange', newPage);
};

const addToPlaylist = song => {
  musicStore.addToPlaylist(song);
  message.success('添加到播放列表成功');
};
// 移除歌曲
const removeSong = (song, index) => {
  musicStore.removeSong(song, index);
  message.success('移除成功');
};
</script>
<style scoped>
.demo-loadmore-list {
  flex: 1;
  overflow: auto;
}
.ranklist-detail {
  flex: 1;
  min-width: 0;
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
  min-height: 60px;
  padding: 0 12px;
  border-radius: 8px;
  transition: all 0.2s;
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
.song-list-container {
  width: 100%;
}
:deep(.anticon-delete:hover) {
  cursor: pointer;
  color: #ff4d4f;
}
:deep(.anticon-plus-circle:hover) {
  cursor: pointer;
  color: #409eff;
}
:deep(.ant-list-item) {
  padding: 8px 0;
}
</style>
