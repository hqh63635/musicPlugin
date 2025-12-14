<script setup>
// 引入组件
import Header from './components/Header.vue';
import MusicBar from './components/MusicBar.vue';
import SideBar from './components/SideBar.vue';
import Lyric from './components/Lyric.vue';
import { useMusicStore } from './store/music.js';
import { ref, onMounted, onBeforeUnmount } from 'vue';

// 使用音乐store
const musicStore = useMusicStore();
const headerStyle = {
  padding: 0,
  background: 'var(--theme-bg-primary)',
  transition: 'background-color 0.3s',
};
const siderStyle = {
  padding: 0,
  background: 'var(--theme-bg-primary)',
  transition: 'background-color 0.3s',
};
const contentStyle = {
  padding: 0,
  background: 'var(--theme-bg-primary)',
  transition: 'background-color 0.3s',
};
const lyricStyle = {
  maxWidth: 'auto',
  minWidth: '0',
  padding: '8px 8px 8px 0',
  background: 'transparent',
};
const footerStyle = {
  padding: 0,
  background: 'var(--theme-bg-primary)',
  transition: 'background-color 0.3s',
};

// 响应式歌词侧边栏宽度
const lyricWidth = ref(400);

// 根据屏幕宽度更新歌词侧边栏宽度
const updateLyricWidth = () => {
  const screenWidth = window.innerWidth;
  if (screenWidth <= 768) {
    lyricWidth.value = '100%';
  } else if (screenWidth <= 992) {
    lyricWidth.value = 350;
  } else if (screenWidth <= 1600) {
    lyricWidth.value = 450;
  } else {
    lyricWidth.value = 600;
  }
};

// 监听窗口大小变化
onMounted(() => {
  updateLyricWidth();
  window.addEventListener('resize', updateLyricWidth);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateLyricWidth);
});
</script>

<template>
  <a-layout style="height: 100vh">
    <a-layout-header :style="headerStyle" height="60px">
      <Header />
    </a-layout-header>
    <a-layout>
      <a-layout-sider :style="siderStyle">
        <SideBar />
      </a-layout-sider>
      <a-layout-content :style="contentStyle">
        <keep-alive>
          <router-view class="main-content" />
        </keep-alive>
      </a-layout-content>
      <a-layout-sider
        class="lyric-container-box"
        :style="lyricStyle"
        :width="lyricWidth"
        v-if="musicStore.showLyricDrawer"
      >
        <Lyric />
      </a-layout-sider>
    </a-layout>
    <a-layout-footer :style="footerStyle" height="80px">
      <MusicBar />
    </a-layout-footer>
  </a-layout>
</template>

<style>
/* 主容器 */
.app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

/* 身体容器 */
.body-container {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* 主内容区域 */
.main-content {
  height: 100%;
  flex: 1;
  min-width: 0;
}

.lyric-container-box {
  width: 600px;
  height: 100%;
  flex-shrink: 0;
  padding: 12px 12px 12px 0;
  background-color: var(--theme-bg-secondary);
  transition: background-color 0.3s;
}
</style>
