<script setup>
// 引入组件
import Header from './components/Header.vue';
import MusicBar from './components/MusicBar.vue';
import SideBar from './components/SideBar.vue';
import Lyric from './components/Lyric.vue';
import { useMusicStore } from './store/music.js';

// 使用音乐store
const musicStore = useMusicStore();
const headerStyle = {
  padding: 0,
  background: '#fff',
};
const siderStyle = {
  padding: 0,
  background: '#fff',
};
const contentStyle = {
  padding: 0,
  background: '#fff',
};
const lyricStyle = {
  maxWidth: 'auto',
  minWidth: '0',
  padding: '8px 8px 8px 0',
  background: 'transparent',
};
const footerStyle = {
  padding: 0,
  background: '#fff',
};
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
        :width="400"
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
  background-color: #f5f5f5;
}

/* 媒体查询 - 中等屏幕 */
@media (max-width: 1600px) {
  .lyric-container-box {
    width: 450px;
  }
}

/* 媒体查询 - 小屏幕 */
@media (max-width: 992px) {
  .lyric-container-box {
    width: 350px;
  }
}

/* 媒体查询 - 超小屏幕 */
@media (max-width: 768px) {
  .lyric-container-box {
    width: 100%;
    padding: 12px;
    order: -1;
  }
}
</style>
