<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Input } from 'ant-design-vue';

// 导入SVG图标
import LogoIcon from '@/assets/icons/logo.svg';
import MagnifyingGlassIcon from '@/assets/icons/magnifying-glass.svg';
import ClockIcon from '@/assets/icons/clock.svg';
import TShirtLineIcon from '@/assets/icons/t-shirt-line.svg';
import Cog8ToothIcon from '@/assets/icons/cog-8-tooth.svg';
import MinusIcon from '@/assets/icons/minus.svg';
import SquareIcon from '@/assets/icons/square.svg';
import XMarkIcon from '@/assets/icons/x-mark.svg';

const router = useRouter();
const inputValue = ref('');
const showSearchHistory = ref(false);
// 主题状态管理
const isDarkTheme = ref(false);

// 模拟搜索历史
const searchHistory = ref(['周杰伦', '五月天', '陈奕迅', 'Taylor Swift']);

// 搜索提交
const onSearchSubmit = () => {
  console.log('onSearchSubmit called, inputValue:', inputValue.value);
  if (inputValue.value) {
    search(inputValue.value);
  }
};

// 执行搜索
const search = keyword => {
  // 导航到搜索页面并传递关键词
  router.push({
    name: 'Search',
    query: { keyword: keyword },
  });
  // 关闭搜索历史
  showSearchHistory.value = false;
};
const handleBlur = () => {
  setTimeout(() => (showSearchHistory.value = false), 200);
};

// 主题切换函数
const toggleTheme = () => {
  isDarkTheme.value = !isDarkTheme.value;
  updateTheme();
};

// 更新主题样式
const updateTheme = () => {
  if (isDarkTheme.value) {
    document.documentElement.classList.add('dark-theme');
    localStorage.setItem('theme', 'dark');
  } else {
    document.documentElement.classList.remove('dark-theme');
    localStorage.setItem('theme', 'light');
  }
};

// 初始化主题
onMounted(() => {
  const savedTheme = localStorage.getItem('theme');
  if (savedTheme === 'dark') {
    isDarkTheme.value = true;
    updateTheme();
  }
});
</script>

<template>
  <div class="header-container">
    <div class="left-part">
      <div class="logo">
        <LogoIcon alt="Logo" />
      </div>

      <!-- 搜索框 -->
      <div id="header-search" class="header-search">
        <Input
          v-model:value="inputValue"
          class="header-search-input"
          placeholder="搜索音乐、歌手、专辑"
          @change="showSearchHistory = true"
          @pressEnter="onSearchSubmit"
          @focus="showSearchHistory = false"
          @blur="handleBlur"
        />
        <div class="search-submit" @click="onSearchSubmit">
          <MagnifyingGlassIcon alt="搜索" />
        </div>

        <!-- 搜索历史 -->
        <div v-if="showSearchHistory && searchHistory.length" class="search-history">
          <div class="history-header">搜索历史</div>
          <div
            v-for="(item, index) in searchHistory.value"
            :key="index"
            class="history-item"
            @click="
              search(item);
              inputValue.value = item;
            "
          >
            <ClockIcon alt="时钟" />
            <span>{{ item }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="right-part">
      <!-- 功能按钮 -->
      <div class="header-button" title="主题" @click="toggleTheme">
        <TShirtLineIcon alt="主题" />
      </div>
      <div class="header-button" title="设置">
        <Cog8ToothIcon alt="设置" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  padding: 0 20px 0 0;
  background-color: var(--theme-bg-primary);
  border-bottom: 1px solid var(--theme-border-primary);
  transition:
    background-color 0.3s,
    border-bottom-color 0.3s;
}

.left-part {
  display: flex;
  align-items: center;
  gap: 20px;
}

.logo {
  width: 205px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: visible;
  color: var(--theme-text-primary) !important;
  transition: color 0.3s !important;
}

.logo svg {
  width: auto;
  max-width: 100%;
  object-fit: contain;
  transform: scale(2);
  fill: currentColor !important;
}

.navigator {
  display: flex;
  gap: 24px;
}

.nav-item {
  color: var(--theme-text-secondary);
  font-size: 16px;
  cursor: pointer;
  transition: color 0.3s;
}

.nav-item:hover {
  color: var(--theme-text-primary);
}

.nav-item.active {
  color: var(--theme-accent-primary);
  font-weight: 500;
}

.header-search {
  position: relative;
  width: 300px;
  height: 36px;
  line-height: 36px;
  background-color: var(--theme-bg-secondary);
  border-radius: 18px;
  overflow: hidden;
  transition: background-color 0.3s;
}

.header-search-input {
  width: 100%;
  height: 100%;
  padding: 0 40px 0 16px;
  background: transparent;
  border: none;
  color: var(--theme-text-primary);
  font-size: 14px;
  outline: none;
  transition: color 0.3s;
}

.header-search-input::placeholder {
  color: var(--theme-text-placeholder);
  transition: color 0.3s;
}

.search-submit {
  position: absolute;
  right: 0;
  top: 0;
  width: 40px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background-color 0.3s;
}

.search-submit:hover {
  background-color: var(--theme-bg-hover);
}

.search-submit svg {
  width: 18px;
  height: 18px;
  opacity: 0.7;
  fill: currentColor !important;
  transition: fill 0.3s !important;
}

.search-submit:hover svg {
  opacity: 1;
}

.search-history {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  max-height: 300px;
  overflow-y: auto;
  background-color: var(--theme-bg-tertiary);
  border-radius: 8px;
  margin-top: 4px;
  box-shadow: 0 4px 12px var(--theme-shadow-primary);
  z-index: 1000;
  transition:
    background-color 0.3s,
    box-shadow 0.3s;
}

.history-header {
  padding: 12px 16px;
  color: var(--theme-text-tertiary);
  font-size: 12px;
  border-bottom: 1px solid var(--theme-border-primary);
  transition:
    color 0.3s,
    border-bottom-color 0.3s;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.history-item:hover {
  background-color: var(--theme-bg-hover);
}

.history-item svg {
  width: 16px;
  height: 16px;
  opacity: 0.6;
  fill: currentColor !important;
  transition:
    opacity 0.3s,
    fill 0.3s !important;
}

.history-item span {
  color: var(--theme-text-primary) !important;
  font-size: 14px;
  transition: color 0.3s !important;
}

.right-part {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-button {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 4px;
  color: var(--theme-text-primary) !important;
  transition:
    background-color 0.3s,
    color 0.3s !important;
}

.header-button:hover {
  background-color: var(--theme-bg-hover);
}

.header-button svg {
  width: 20px;
  height: 20px;
  opacity: 0.7;
  fill: currentColor !important;
  transition:
    opacity 0.3s,
    fill 0.3s !important;
}

.header-button:hover svg {
  opacity: 1;
}

.header-divider {
  width: 1px;
  height: 24px;
  background-color: var(--theme-border-primary);
  transition: background-color 0.3s;
}
</style>
