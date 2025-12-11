<template>
  <div class="lyric-container">
    <div class="content-container">
      <div class="lyric-text-row" :style="{
          left: left + 'px',
          transition: enableTransition ? 'left 900ms linear' : 'none'
        }">
        {{ displayText }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useMusicStore } from '@/store/music';
import api from '@/services/api';
import axios from 'axios';

const musicStore = useMusicStore();

// 响应式状态
const enableTransition = ref(false);
const left = ref(null);

// 计算属性
const displayText = computed(() => {
  if (musicStore.parsedLrc?.lrc) {
    return musicStore.parsedLrc.lrc;
  } else if (musicStore.currentSong) {
    return `${musicStore.currentSong.title} - ${
      musicStore.currentSong.singerName || musicStore.currentSong.artist
    }`;
  }
  return '暂无歌词';
});

const textWidth = computed(() => {
  const text = displayText.value;
  if (!text) return 0;

  // 创建临时元素来测量文本宽度
  const tempElement = document.createElement('div');
  tempElement.style.position = 'absolute';
  tempElement.style.visibility = 'hidden';
  tempElement.style.whiteSpace = 'nowrap';
  tempElement.style.fontSize = '24px'; // 使用与组件相同的字体大小
  tempElement.style.fontFamily = 'Arial, sans-serif'; // 使用默认字体
  tempElement.textContent = text;

  document.body.appendChild(tempElement);
  const width = tempElement.offsetWidth;
  document.body.removeChild(tempElement);

  return width;
});

// 监听文本宽度变化，调整布局
watch(textWidth, newWidth => {
  if (newWidth > window.innerWidth) {
    enableTransition.value = false;
    left.value = 0;
  } else {
    left.value = null;
  }
});

// 监听当前播放时间变化，更新歌词和滚动位置
watch(
  () => musicStore.currentTime,
  newTime => {
    // 更新当前歌词
    musicStore.updateCurrentLyric(newTime);

    // 处理长文本滚动
    if (textWidth.value > window.innerWidth && musicStore.fullLyric.length > 0) {
      const currentIndex = musicStore.currentLyricIndex;
      if (currentIndex > -1 && currentIndex < musicStore.fullLyric.length - 1) {
        const currentLrc = musicStore.fullLyric[currentIndex];
        const nextLrc = musicStore.fullLyric[currentIndex + 1];

        if (nextLrc.time > currentLrc.time) {
          const diff = nextLrc.time - currentLrc.time;
          const progress = (newTime - currentLrc.time) / diff;
          const scrollAmount = progress * textWidth.value;

          if (scrollAmount > window.innerWidth * 0.5) {
            enableTransition.value = true;
            left.value = -Math.min(
              (scrollAmount - window.innerWidth * 0.5) * 1.1,
              textWidth.value - window.innerWidth
            );
            return;
          }
        }
      }

      enableTransition.value = false;
      left.value = 0;
    }
  }
);

// 监听当前歌曲变化，重置歌词位置并获取歌词
watch(
  () => musicStore.currentSong,
  async newSong => {
    enableTransition.value = false;
    left.value = null;

    if (newSong) {
      try {
        // 从API获取歌词
        const lyricResult = await api.getLyric(newSong);
        if (
          lyricResult &&
          typeof lyricResult.lyric === 'string' &&
          lyricResult.lyric.trim() !== ''
        ) {
          musicStore.setLyric(lyricResult.lyric);
        } else {
          // 如果没有获取到有效歌词，生成模拟歌词
          const mockLyric = `[00:00.00]${newSong.title}[00:10.00]演唱：${
            newSong.singerName || newSong.artist
          }[00:20.00]暂无歌词[00:30.00]请欣赏音乐`;
          musicStore.setLyric(mockLyric);
        }
      } catch (error) {
        console.error('获取歌词失败:', error);
        // 请求失败时使用模拟歌词
        const mockLyric = `[00:00.00]${newSong.title}[00:10.00]演唱：${
          newSong.singerName || newSong.artist
        }[00:20.00]暂无歌词[00:30.00]请欣赏音乐`;
        musicStore.setLyric(mockLyric);
      }
    } else {
      musicStore.setLyric('');
    }
  }
);

// 组件挂载时初始化
onMounted(async () => {
  // 初始化时如果有当前歌曲，获取真实歌词
  if (musicStore.currentSong) {
    try {
      const lyricResult = await api.getLyric(musicStore.currentSong);
      if (lyricResult && typeof lyricResult.lyric === 'string' && lyricResult.lyric.trim() !== '') {
        musicStore.setLyric(lyricResult.lyric);
      } else {
      }
    } catch (error) {
      console.error('获取歌词失败:', error);
      // 请求失败时使用模拟歌词
      
    }
  }
});
</script>

<style scoped>
.lyric-container {
  position: relative;
  width: 100%;
  height: 100%;
  cursor: default;
  user-select: none;
}

.content-container {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.lyric-text-row {
  -webkit-text-stroke: 1px #b48f1d;
  color: white;
  font-size: 24px;
  white-space: nowrap;
  position: absolute;
  text-align: center;
  max-width: 100%;
}
</style>