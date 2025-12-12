<template>
  <div class="song-table-wrapper">
    <vxe-table
      ref="xTable"
      :data="listSongs"
      height="100%"
      border="none"
      row-config="{ isHover: true }"
      :scroll-y="{ enabled: true, gt: 10 }"
      @scroll="handleScroll"
    >
      <!-- 序号 -->
      <vxe-column title="序号" width="60">
        <template #default="{ rowIndex }">
          {{ rowIndex + 1 }}
        </template>
      </vxe-column>

      <!-- 歌曲 -->
      <vxe-column title="歌曲">
        <template #default="{ row }">
          <div class="song-info">
            <img class="cover" :src="row.artwork?.replace(/[`\s]/g, '') || defaultCover" alt="" />
            <span class="title">{{ row.title }}</span>
            <PlayCircleOutlined class="play-btn" @click="playSong(row)" />
          </div>
        </template>
      </vxe-column>

      <!-- 歌手 -->
      <vxe-column title="歌手" field="artist" />

      <!-- 操作 -->
      <vxe-column title="操作" width="80">
        <template #default="{ row, rowIndex }">
          <slot name="actions" :item="row" :index="rowIndex"></slot>

          <PlusCircleOutlined v-if="isShowAdd" class="icon add" @click="addToPlaylist(row)" />
          <DeleteOutlined
            v-if="isShowDelete"
            class="icon delete"
            @click="removeSong(row, rowIndex)"
          />
        </template>
      </vxe-column>
    </vxe-table>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { PlayCircleOutlined, PlusCircleOutlined, DeleteOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { useMusicStore } from '@/store/music.js';

const musicStore = useMusicStore();
const props = defineProps({
  listSongs: Array,
  isShowAdd: Boolean,
  isShowDelete: Boolean,
  isEnd: Boolean,
  currentPage: Number,
});
const emit = defineEmits(['pageChange']);

const defaultCover = '@/assets/default-cover.jpg';
const xTable = ref(null);

// 播放歌曲
const playSong = song => {
  musicStore.playSong(song);
};

// 触底自动加载更多
const handleScroll = ({ scrollTop, $table }) => {
  const scrollInfo = $table.getScroll();
  const { scrollHeight, clientHeight } = scrollInfo;

  // 判断是否滚动到底
  if (scrollTop + clientHeight >= scrollHeight - 10) {
    onLoadMore();
  }
};

const onLoadMore = () => {
  if (props.isEnd) return;
  emit('pageChange', props.currentPage + 1);
};

// 添加歌曲
const addToPlaylist = song => {
  musicStore.addToPlaylist(song);
  message.success('添加到播放列表成功');
};

// 删除歌曲
const removeSong = (song, index) => {
  musicStore.removeSong(song, index);
  message.success('移除成功');
};
</script>

<style scoped>
/* .song-table-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.cover {
  width: 50px;
  height: 50px;
  border-radius: 6px;
  margin-right: 12px;
}
.song-info {
  display: flex;
  align-items: center;
}
.play-btn {
  margin-left: 10px;
  cursor: pointer;
  font-size: 18px;
}
.icon {
  cursor: pointer;
  font-size: 18px;
  margin-left: 10px;
}
.icon.add:hover {
  color: #409eff;
}
.icon.delete:hover {
  color: #ff4d4f;
}
.load-more {
  text-align: center;
  padding: 16px;
} */
.song-table-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
}

/* ------------ 行 Hover 效果（网易云风格） ------------ */
:deep(.vxe-body--row:hover) {
  background: #f5f7fa !important;
  transition: background 0.25s ease;
}

/* 序号样式（网易云：灰色 -> hover 红色） */
:deep(.vxe-body--row:hover .song-index) {
  color: #ec4141 !important;
}

/* 行内布局 */
.song-info {
  display: flex;
  align-items: center;
  min-width: 0;
}

/* 封面图（网易云风格：圆角 + hover 放大） */
.cover {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  margin-right: 12px;
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease;
}

.song-info:hover .cover {
  transform: scale(1.06);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
}

/* 歌曲名字（文本溢出省略） */
.title {
  flex: 1;
  font-size: 15px;
  font-weight: 500;
  color: #333;
  transition: color 0.2s ease;
  min-width: 0;
}

.song-info:hover .title {
  color: #000;
}

/* 播放按钮默认隐藏，hover 逐渐出现 */
.play-btn {
  font-size: 20px;
  margin-left: 10px;
  opacity: 0;
  cursor: pointer;
  transition:
    opacity 0.25s ease,
    transform 0.25s ease;
}

.song-info:hover .play-btn {
  opacity: 1;
  transform: scale(1.1);
  color: #ec4141;
}

/* 操作图标样式 */
.icon {
  cursor: pointer;
  font-size: 18px;
  margin-left: 10px;
  transition:
    color 0.2s ease,
    transform 0.2s ease;
}

/* 添加按钮 hover 蓝色（QQ 音乐风格） */
.icon.add:hover {
  color: #31c27c;
  transform: scale(1.15);
}

/* 删除按钮 hover 红色 */
.icon.delete:hover {
  color: #ff4d4f;
  transform: scale(1.15);
}

/* 加载更多按钮 */
.load-more {
  text-align: center;
  padding: 16px;
}

/* VXE 列头样式（贴近网易云） */
:deep(.vxe-header--row .vxe-header--column) {
  font-size: 14px;
  font-weight: 600;
  color: #666;
  background: #fafafa;
}
</style>
