<template>
  <div class="layout-container">
    <router-view />
  </div>
</template>

<script setup>
// 这是一个简单的容器组件，用于渲染artist相关的子路由
</script>

<style scoped>
.layout-container {
  width: 100%;
  height: 100%;
  background-color: var(--theme-bg-primary);
  transition: background-color 0.3s;
}
</style>
