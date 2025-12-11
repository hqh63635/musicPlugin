<template>
  <div class="lyric-container">
    <div class="lyric-scroll-container" ref="scrollContainer">
      <div class="lyric-list">
        <div
          v-for="(line, index) in musicStore.fullLyric"
          :key="index"
          class="lyric-line"
          :class="{ 'active': index === musicStore.currentLyricIndex }"
          :data-time="line.time"
        >
          {{ line.lrc }}
        </div>
        <div v-if="musicStore.fullLyric.length === 0" class="no-lyric">
          {{ displayText }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue';
import { useMusicStore } from '@/store/music';

const musicStore = useMusicStore();
const scrollContainer = ref(null);

const displayText = computed(() => {
  if (musicStore.currentSong) {
    return `${musicStore.currentSong.title} - ${musicStore.currentSong.singerName || musicStore.currentSong.artist}`;
  }
  return '暂无歌词';
});

// 监听当前歌词索引变化，实现滚动居中
watch(
  () => musicStore.currentLyricIndex,
  (newIndex) => {
    if (newIndex === -1 || !scrollContainer.value) return;

    nextTick(() => {
      const activeLine = scrollContainer.value.querySelector('.lyric-line.active');
      if (activeLine) {
        const container = scrollContainer.value;
        const containerRect = container.getBoundingClientRect();
        const lineRect = activeLine.getBoundingClientRect();
        const scrollTop = container.scrollTop + (lineRect.top - containerRect.top - containerRect.height / 2 + lineRect.height / 2);

        container.scrollTo({
          top: scrollTop,
          behavior: 'smooth'
        });
      }
    });
  }
);
</script>

<style scoped>
.lyric-container {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.lyric-scroll-container {
  width: 100%;
  height: 100%;
  overflow: auto;
  padding: 20px 0;
}

.lyric-list {
  text-align: center;
  min-height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.lyric-line {
  color: rgba(255, 255, 255, 0.7);
  font-size: 16px;
  line-height: 1.8;
  transition: all 0.3s ease;
  padding: 4px 0;
}

.lyric-line.active {
  color: #ffd700;
  font-size: 18px;
  font-weight: bold;
  text-shadow: 0 0 8px rgba(255, 215, 0, 0.6);
}

.no-lyric {
  color: white;
  font-size: 24px;
  text-align: center;
}
</style>