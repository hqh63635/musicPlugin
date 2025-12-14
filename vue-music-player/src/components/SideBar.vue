<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import MusicSheet from './MusicSheet.vue';

import HeartIcon from '@/assets/icons/heart.svg';
import ClockIcon from '@/assets/icons/clock.svg';
import FolderOpenIcon from '@/assets/icons/folder-open.svg';
import ArrayDownloadTrayIcon from '@/assets/icons/array-download-tray.svg';
import ListBulletIcon from '@/assets/icons/list-bullet.svg';
import AlbumIcon from '@/assets/icons/album.svg';
import UserIcon from '@/assets/icons/user.svg';
import DocumentPlusIcon from '@/assets/icons/document-plus.svg';
import IdentificationIcon from '@/assets/icons/identification.svg';
import TrophyIcon from '@/assets/icons/trophy.svg';

const router = useRouter();

// 侧边栏导航项
const navItems = ref([
  { icon: IdentificationIcon, text: '搜索', path: '/search' },
  { icon: TrophyIcon, text: '排行榜', path: '/ranklist' },
  { icon: HeartIcon, text: '我喜欢的音乐', path: '/favorite' },
  { icon: ClockIcon, text: '最近播放', path: '/recent' },
  { icon: ArrayDownloadTrayIcon, text: '下载管理', path: '/download' },
  { icon: ListBulletIcon, text: '我的歌单', path: '/playlist' },
  { icon: AlbumIcon, text: '专辑', path: '/album' },
  { icon: UserIcon, text: '歌手', path: '/artist' },
]);

// 切换导航项
const switchNavItem = index => {
  const item = navItems.value[index];
  if (item.path) {
    router.push(item.path);
  }
  navItems.value.forEach((navItem, i) => {
    navItem.active = i === index;
  });
};
</script>

<template>
  <div class="sidebar-container">
    <!-- 导航菜单 -->
    <div class="nav-menu">
      <router-link
        v-for="(item, index) in navItems"
        :key="index"
        :to="item.path"
        class="nav-item"
        active-class="active"
      >
        <div class="nav-icon">
          <component :is="item.icon" :alt="item.text" />
        </div>
        <div class="nav-text">{{ item.text }}</div>
      </router-link>
    </div>
    <!-- 歌单列表 -->
    <MusicSheet />
  </div>
</template>

<style scoped>
.sidebar-container {
  width: 100%;
  height: 100%;
  background-color: var(--theme-bg-primary);
  border-right: 1px solid var(--theme-border-primary);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  transition: width 0.3s, background-color 0.3s, border-right-color 0.3s;
}
.sidebar-container::-webkit-scrollbar {
  width: 0;
  height: 0;
  background: transparent;
}

/* Firefox */
.sidebar-container {
  scrollbar-width: none;
}

/* IE/Edge */
.sidebar-container {
  -ms-overflow-style: none;
}
/* 导航菜单 */
.nav-menu {
  padding: 12px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.nav-item:hover {
  background-color: var(--theme-bg-hover);
}

.nav-item.active {
  background-color: var(--theme-bg-active);
  border-right: 3px solid var(--theme-accent-primary);
}

.nav-icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-icon svg {
  width: 24px;
  height: 24px;
  opacity: 0.7;
  transition: opacity 0.3s;
}

.nav-item:hover .nav-icon svg,
.nav-item.active .nav-icon svg {
  opacity: 1;
}

.nav-text {
  font-size: 14px;
  color: var(--theme-text-primary);
  transition: color 0.3s;
}

.nav-item.active .nav-text {
  color: var(--theme-accent-primary);
}



/* 响应式设计 */
@media (max-width: 1024px) {
  .sidebar-container {
    width: 80px;
  }

  .user-name,
  .login-button,
  .nav-text,
  .section-header,
  .playlist-info {
    display: none;
  }

  .user-avatar {
    width: 40px;
    height: 40px;
    margin: 0 auto;
  }

  .user-avatar svg {
    width: 24px;
    height: 24px;
  }

  .nav-item {
    justify-content: center;
    padding: 12px 0;
  }

  .playlist-item {
    justify-content: center;
    padding: 8px 0;
  }

  .playlist-cover {
    width: 40px;
    height: 40px;
  }
}
</style>
