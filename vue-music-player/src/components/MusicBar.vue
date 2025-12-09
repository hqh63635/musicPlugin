<script setup>
import { ref, onMounted, onBeforeUnmount } from "vue";
import { useMusicStore } from "@/store/music.js";
import albumCover from "@/assets/imgs/album-cover.jpg";
import repeatSongIcon from "@/assets/icons/repeat-song.svg";
import repeatSong1Icon from "@/assets/icons/repeat-song-1.svg";
import shuffleIcon from "@/assets/icons/shuffle.svg";
import skipLeftIcon from "@/assets/icons/skip-left.svg";
import playIcon from "@/assets/icons/play.svg";
import pauseIcon from "@/assets/icons/pause.svg";
import skipRightIcon from "@/assets/icons/skip-right.svg";
import speakerXMarkIcon from "@/assets/icons/speaker-x-mark.svg";
import speakerWaveIcon from "@/assets/icons/speaker-wave.svg";
import lyricIcon from "@/assets/icons/lyric.svg";
import heartOutlineIcon from "@/assets/icons/heart-outline.svg";
import listBulletIcon from "@/assets/icons/list-bullet.svg";

// 使用音乐store
const musicStore = useMusicStore();

// 音频元素引用
const audioRef = ref(null);

// 组件挂载时初始化
onMounted(() => {
  // 设置音频元素
  musicStore.setAudioElement(audioRef.value);
  // 绑定事件监听
  audioRef.value.addEventListener("timeupdate", handleTimeUpdate);
  audioRef.value.addEventListener("loadedmetadata", handleLoadedMetadata);
  audioRef.value.addEventListener("ended", handleEnded);

  // 初始化歌曲信息
  if (!musicStore.currentSong) {
    musicStore.playSong({
    id: 102340965,
    songmid: '004Yi5BD3ksoAN',
    title: '蒲公英的约定',
    artist: '周杰伦',
    artwork: 'https://y.gtimg.cn/music/photo_new/T002R800x800M000002eFUFm2XYZ7z.jpg',
    album: '我很忙',
    lrc: undefined,
    albumid: 33021,
    albummid: '002eFUFm2XYZ7z'
  });
  }
});

// 组件卸载前清理
onBeforeUnmount(() => {
  // 移除事件监听
  if (audioRef.value) {
    audioRef.value.removeEventListener("timeupdate", handleTimeUpdate);
    audioRef.value.removeEventListener("loadedmetadata", handleLoadedMetadata);
    audioRef.value.removeEventListener("ended", handleEnded);
  }
});

// 处理时间更新
const handleTimeUpdate = () => {
  musicStore.updateCurrentTime(audioRef.value.currentTime);
};

// 处理元数据加载完成
const handleLoadedMetadata = () => {
  musicStore.updateTotalTime(audioRef.value.duration);
};

// 处理播放结束
const handleEnded = () => {
  musicStore.onEnded();
};

// 格式化时间
const formatTime = (seconds) => {
  return musicStore.formatTime(seconds);
};

// 切换播放状态
const togglePlay = () => {
  musicStore.togglePlay();
};

// 上一首
const previousSong = () => {
  musicStore.previousSong();
};

// 下一首
const nextSong = () => {
  musicStore.nextSong();
};

// 切换音量静音
const toggleMute = () => {
  musicStore.toggleMute();
};

// 调整音量
const adjustVolume = (newVolume) => {
  musicStore.setVolume(newVolume);
};

// 调整播放进度
const adjustProgress = (event) => {
  const rect = event.currentTarget.getBoundingClientRect();
  const clickX = event.clientX - rect.left;
  const percentage = (clickX / rect.width) * 100;
  musicStore.setProgress(percentage);
};

// 切换播放模式
const togglePlayMode = () => {
  musicStore.setPlayMode((musicStore.playMode + 1) % 3);
};
</script>

<template>
  <div class="music-bar-container">
    <!-- 音频元素 -->
    <audio ref="audioRef" src="https://lv-sycdn.kuwo.cn/27602873ff00ab3b26d7bf4f2bf61417/6936f511/resource/30106/trackmedia/M800004Yi5BD3ksoAN.mp3"/>

    <!-- 当前播放歌曲信息 -->
    <div class="music-info">
      <div class="music-cover">
        <img
          :src="musicStore?.currentSong?.artwork || albumCover"
          :alt="musicStore?.currentSong?.name || '未知歌曲'"
        />
      </div>
      <div class="music-details">
        <div class="music-name">
          {{ musicStore?.currentSong?.title || "未知歌曲" }}
        </div>
        <div class="music-artist">
          {{ musicStore?.currentSong?.artist || "未知歌手" }}
        </div>
      </div>
    </div>

    <!-- 播放控制 -->
    <div class="player-controller">
      <!-- 进度条 -->
      <div class="progress-bar">
        <div class="time-info current-time">
          {{ formatTime(musicStore.currentTime) }}
        </div>
        <div class="progress-container" @click="adjustProgress">
          <div class="progress-track">
            <div
              class="progress-fill"
              :style="{ width: musicStore.progressPercentage + '%' }"
            ></div>
            <div
              class="progress-thumb"
              :style="{ left: musicStore.progressPercentage + '%' }"
            ></div>
          </div>
        </div>
        <div class="time-info total-time">
          {{ formatTime(musicStore.totalTime) }}
        </div>
      </div>

      <!-- 控制按钮 -->
      <div class="control-buttons">
        <!-- 播放模式 -->
        <div class="control-button play-mode-button" @click="togglePlayMode">
          <component
            :is="
              musicStore.playMode === 0
                ? repeatSongIcon
                : musicStore.playMode === 1
                ? repeatSong1Icon
                : shuffleIcon
            "
            :alt="
              musicStore.playMode === 0
                ? '列表循环'
                : musicStore.playMode === 1
                ? '单曲循环'
                : '随机播放'
            "
          />
        </div>

        <!-- 上一首 -->
        <div class="control-button" @click="previousSong">
          <component :is="skipLeftIcon" :alt="'上一首'" />
        </div>

        <!-- 播放/暂停 -->
        <div class="control-button play-button" @click="togglePlay">
          <component
            :is="musicStore.isPlaying ? pauseIcon : playIcon"
            :alt="musicStore.isPlaying ? '暂停' : '播放'"
          />
        </div>

        <!-- 下一首 -->
        <div class="control-button" @click="nextSong">
          <component :is="skipRightIcon" :alt="'下一首'" />
        </div>
      </div>
    </div>

    <!-- 音量控制 -->
    <div class="volume-control">
      <div class="control-button" @click="toggleMute">
        <component
          :is="
            musicStore.isMuted || musicStore.volume === 0
              ? speakerXMarkIcon
              : speakerWaveIcon
          "
          :alt="musicStore.isMuted ? '静音' : '音量'"
        />
      </div>
      <div class="volume-slider">
        <div class="progress-track">
          <div
            class="progress-fill"
            :style="{ width: musicStore.volume + '%' }"
          ></div>
          <div
            class="progress-thumb"
            :style="{ left: musicStore.volume + '%' }"
          ></div>
        </div>
      </div>
      <div class="control-button">
        <component :is="lyricIcon" :alt="'歌词'" />
      </div>
      <div class="control-button">
        <component :is="heartOutlineIcon" :alt="'喜欢'" />
      </div>
      <div class="control-button">
        <component :is="listBulletIcon" :alt="'播放列表'" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.music-bar-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 80px;
  padding: 0 20px;
  background-color: #ffffff;
  border-top: 1px solid #e0e0e0;
  gap: 20px;
}

/* 音乐信息 */
.music-info {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 200px;
}

.music-cover {
  width: 56px;
  height: 56px;
  border-radius: 4px;
  overflow: hidden;
}

.music-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.music-details {
  min-width: 0;
}

.music-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.music-artist {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 播放控制器 */
.player-controller {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 600px;
}

.progress-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.time-info {
  font-size: 12px;
  color: #999;
  width: 40px;
}

.progress-container {
  flex: 1;
  height: 4px;
}

.progress-track {
  position: relative;
  width: 100%;
  height: 100%;
  background-color: #e0e0e0;
  border-radius: 2px;
  cursor: pointer;
}

.progress-fill {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background-color: #1890ff;
  border-radius: 2px;
  transition: width 0.3s;
}

.progress-thumb {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 12px;
  height: 12px;
  background-color: #fff;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  cursor: pointer;
  transition: transform 0.2s;
}

.progress-thumb:hover {
  transform: translate(-50%, -50%) scale(1.2);
}

.control-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.control-button {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 50%;
  transition: background-color 0.3s;
}

.control-button:hover {
  background-color: #f5f5f5;
}

.control-button svg {
  width: 20px;
  height: 20px;
  opacity: 0.8;
  transition: opacity 0.3s;
  display: block;
  margin: auto;
}

.play-mode-button svg {
  width: auto;
  height: auto;
}

.control-button:hover svg {
  opacity: 1;
}

.play-button {
  width: 44px;
  height: 44px;
  background-color: #1890ff;
}

.play-button:hover {
  background-color: #40a9ff;
}

.play-button svg {
  width: 24px;
  height: 24px;
  opacity: 1;
  display: block;
  margin: auto;
}

/* 音量控制 */
.volume-control {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 200px;
}

.volume-slider {
  width: 80px;
  height: 4px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .music-bar-container {
    padding: 0 10px;
    gap: 8px;
  }

  .music-info {
    min-width: 150px;
    gap: 8px;
  }

  .music-cover {
    width: 48px;
    height: 48px;
  }

  .music-name {
    font-size: 13px;
  }

  .music-artist {
    font-size: 11px;
  }

  .player-controller {
    max-width: 400px;
    gap: 8px;
  }

  .progress-bar {
    gap: 8px;
  }

  .time-info {
    font-size: 11px;
    width: 35px;
  }

  .control-buttons {
    gap: 12px;
  }

  .control-button {
    width: 32px;
    height: 32px;
  }

  .control-button svg {
    width: 18px;
    height: 18px;
  }

  .play-button {
    width: 40px;
    height: 40px;
  }

  .play-button svg {
    width: 22px;
    height: 22px;
  }

  .volume-control {
    min-width: 150px;
    gap: 8px;
  }

  .volume-slider {
    width: 60px;
  }
}

@media (max-width: 768px) {
  .music-bar-container {
    padding: 0 8px;
    gap: 4px;
    height: 70px;
  }

  .music-info {
    min-width: 120px;
    gap: 6px;
  }

  .music-cover {
    width: 40px;
    height: 40px;
  }

  .music-name {
    font-size: 12px;
  }

  .music-artist {
    font-size: 10px;
  }

  .player-controller {
    flex: 1;
    max-width: none;
    gap: 6px;
  }

  .progress-bar {
    gap: 6px;
  }

  .time-info {
    font-size: 10px;
    width: 30px;
  }

  .control-buttons {
    gap: 8px;
  }

  .control-button {
    width: 28px;
    height: 28px;
  }

  .control-button svg {
    width: 16px;
    height: 16px;
  }

  .play-button {
    width: 36px;
    height: 36px;
  }

  .play-button svg {
    width: 20px;
    height: 20px;
  }

  .volume-control {
    min-width: auto;
    gap: 6px;
  }

  .volume-slider {
    display: none;
  }

  /* 隐藏部分音量控制按钮 */
  .volume-control .control-button:nth-child(3),
  .volume-control .control-button:nth-child(4),
  .volume-control .control-button:nth-child(5) {
    display: none;
  }
}
</style>