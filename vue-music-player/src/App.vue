<script setup>
// 引入组件
import Header from './components/Header.vue'
import MusicBar from './components/MusicBar.vue'
import SideBar from './components/SideBar.vue'
import MusicDetail from './components/MusicDetail.vue'
import Lyric from './components/Lyric.vue'
</script>

<template>
  <div class="app-container">
    <Header />
    <div class="body-container">
      <SideBar />
      <router-view class="main-content" />
      <!-- <Panel /> -->
       <div class="lyric-container-box"> 
         <Lyric />
        </div>
    </div>
    <!-- <MusicDetail /> -->
    <MusicBar />
  </div>
</template>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #ffffff;
  color: #333333;
}

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
  flex: 1;
}
.lyric-container-box {
  width: 600px;
  padding: 12px;
  background-color: #f5f5f5;
}
</style>
