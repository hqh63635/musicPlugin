<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

// 导入SVG图标
import CdIcon from '@/assets/icons/cd.svg'
import HeartIcon from '@/assets/icons/heart.svg'
import ClockIcon from '@/assets/icons/clock.svg'
import FolderOpenIcon from '@/assets/icons/folder-open.svg'
import ArrayDownloadTrayIcon from '@/assets/icons/array-download-tray.svg'
import ListBulletIcon from '@/assets/icons/list-bullet.svg'
import AlbumIcon from '@/assets/icons/album.svg'
import UserIcon from '@/assets/icons/user.svg'
import DocumentPlusIcon from '@/assets/icons/document-plus.svg'
import IdentificationIcon from '@/assets/icons/identification.svg'
import TrophyIcon from '@/assets/icons/trophy.svg'

const router = useRouter()

// 侧边栏导航项
const navItems = ref([
  { icon: TrophyIcon, text: '排行榜', active: true, path: '/ranklist' },
  // { icon: CdIcon, text: '我的音乐', active: true, path: '/' },
  { icon: HeartIcon, text: '我喜欢的音乐', path: '/favorite' },
  { icon: ClockIcon, text: '最近播放', path: '/recent' },
  // { icon: FolderOpenIcon, text: '本地音乐', path: '/local' },
  { icon: ArrayDownloadTrayIcon, text: '下载管理', path: '/download' },
  { icon: ListBulletIcon, text: '我的歌单', path: '/playlist' },
  { icon: AlbumIcon, text: '专辑', path: '/album' },
  { icon: UserIcon, text: '歌手', path: '/artist' },
])

// 切换导航项
const switchNavItem = (index) => {
  const item = navItems.value[index]
  if (item.path) {
    router.push(item.path)
  }
  navItems.value.forEach((navItem, i) => {
    navItem.active = i === index
  })
}</script>

<template>
  <div class="sidebar-container">
    
    <!-- 导航菜单 -->
    <div class="nav-menu">
      <div 
        v-for="(item, index) in navItems" 
        :key="index"
        class="nav-item"
        :class="{ active: item.active }"
        @click="switchNavItem(index)"
      >
        <div class="nav-icon">
          <component :is="item.icon" :alt="item.text" />
        </div>
        <div class="nav-text">{{ item.text }}</div>
      </div>
    </div>
    
    <!-- 歌单列表 -->
    <div class="playlist-section">
      <div class="section-header">
          <span>我的歌单</span>
          <DocumentPlusIcon alt="添加" />
        </div>
      <div class="playlist-list">
        <div class="playlist-item">
          <div class="playlist-cover">
            <CdIcon alt="歌单封面" />
          </div>
          <div class="playlist-info">
            <div class="playlist-name">我创建的歌单</div>
            <div class="playlist-count">0首</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.sidebar-container {
  width: 240px;
  height: 100%;
  background-color: #ffffff;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  transition: width 0.3s;
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
  background-color: #f5f5f5;
}

.nav-item.active {
  background-color: #e6f7ff;
  border-right: 3px solid #1890ff;
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
  color: #333;
}

.nav-item.active .nav-text {
  color: #1890ff;
}

/* 歌单列表 */
.playlist-section {
  flex: 1;
  padding: 16px 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header span {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.section-header svg {
  width: 18px;
  height: 18px;
  opacity: 0.6;
  cursor: pointer;
  transition: opacity 0.3s;
}

.section-header svg:hover {
  opacity: 1;
}

.playlist-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.playlist-item {
  display: flex;
  gap: 12px;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.playlist-item:hover {
  background-color: #f5f5f5;
}

.playlist-cover {
  width: 48px;
  height: 48px;
  background-color: #f0f0f0;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.playlist-cover svg {
  width: 24px;
  height: 24px;
  opacity: 0.6;
}

.playlist-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
}

.playlist-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.playlist-count {
  font-size: 12px;
  color: #999;
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