<template>
  <div class="lyric-container">
    <div
      class="lyric-scroll-container"
      ref="scrollContainer"
      @wheel.prevent="onWheel"
      @mousedown="onMouseDown"
      @mousemove="onMouseMove"
      @mouseup="onMouseUp"
      @mouseleave="onMouseUp"
      :style="
        musicStore?.currentSong?.artwork
          ? {
              backgroundImage: `url(${musicStore.currentSong.artwork})`,
              backgroundSize: 'cover',
              backgroundPosition: 'center',
              backgroundRepeat: 'no-repeat',
            }
          : {
              backgroundColor: 'var(--theme-bg-primary)',
            }
      "
    >
      <div class="lyric-list">
        <div
          v-for="(line, index) in musicStore.fullLyric"
          :key="index"
          class="lyric-line"
          :class="{ active: index === musicStore.currentLyricIndex }"
          :data-time="line.time"
        >
          {{ line.lrc }}
        </div>
        <div v-if="musicStore.fullLyric.length === 0" class="no-lyric">
          {{ t('lyric.noLyric') }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue';
import { useMusicStore } from '@/store/music';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const musicStore = useMusicStore();
const scrollContainer = ref(null);

const displayText = computed(() => {
  if (musicStore.currentSong) {
    return `${musicStore.currentSong.title} - ${
      musicStore.currentSong.singerName || musicStore.currentSong.artist
    }`;
  }
  return t('lyric.noLyric');
});

// 监听当前歌词索引变化，实现滚动居中
watch(
  () => musicStore.currentLyricIndex,
  newIndex => {
    if (newIndex === -1 || !scrollContainer.value) return;

    nextTick(() => {
      const activeLine = scrollContainer.value.querySelector('.lyric-line.active');
      if (activeLine) {
        const container = scrollContainer.value;
        const containerRect = container.getBoundingClientRect();
        const lineRect = activeLine.getBoundingClientRect();
        const scrollTop =
          container.scrollTop +
          (lineRect.top - containerRect.top - containerRect.height / 2 + lineRect.height / 2);

        container.scrollTo({
          top: scrollTop,
          behavior: 'smooth',
        });
      }
    });
  }
);

const isDown = ref(false);
let startY = 0;
let scrollStart = 0;

let lastY = 0;
let velocity = 0;
let momentumFrame = null;

const stopMomentum = () => cancelAnimationFrame(momentumFrame);

// ----------------------
// 按住拖动
// ----------------------
const onMouseDown = e => {
  isDown.value = true;
  startY = e.clientY;
  scrollStart = scrollContainer.value.scrollTop;
  stopMomentum();
  velocity = 0;
  lastY = e.clientY;
};

const onMouseMove = e => {
  if (!isDown.value) return;
  const deltaY = e.clientY - startY;
  scrollContainer.value.scrollTop = scrollStart - deltaY;

  velocity = e.clientY - lastY;
  lastY = e.clientY;
};

const onMouseUp = () => {
  if (!isDown.value) return;
  isDown.value = false;
  applyMomentum();
};

// ----------------------
// 鼠标滚轮惯性滚动
// ----------------------
const onWheel = e => {
  stopMomentum();
  velocity += e.deltaY * 0.1; // 滚轮量适配
  applyMomentum();
};

// ----------------------
// 惯性滚动实现
// ----------------------
const applyMomentum = () => {
  const container = scrollContainer.value;
  if (!container) return;

  const friction = 0.92; // 滑动衰减
  const minVelocity = 0.3; // 停止阈值

  const step = () => {
    container.scrollTop += velocity;
    velocity *= friction;

    if (Math.abs(velocity) > minVelocity) {
      momentumFrame = requestAnimationFrame(step);
    }
  };

  momentumFrame = requestAnimationFrame(step);
};
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
  overflow: hidden;
  padding: 20px 0;
  background-size: cover;
  border-radius: 12px;
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

.dark-theme .lyric-line {
  color: rgba(255, 255, 255, 0.7);
}

.lyric-line.active {
  color: var(--theme-accent-primary);
  font-size: 24px;
  font-weight: bold;
  text-shadow: 0 0 8px var(--theme-accent-primary);
}

.no-lyric {
  color: var(--theme-text-primary);
  font-size: 24px;
  text-align: center;
}
</style>
