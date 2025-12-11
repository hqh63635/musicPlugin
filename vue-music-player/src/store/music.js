import { defineStore } from 'pinia';
import { ref, computed, watch } from 'vue';
import api from '@/services/api.js';

export const useMusicStore = defineStore(
  'music',
  () => {
    // 当前播放歌曲信息
    const currentSong = ref(null);
    // 播放状态
    const isPlaying = ref(false);
    // 当前播放进度（秒）
    const currentTime = ref(0);
    // 总时长（秒）
    const totalTime = ref(0);
    // 播放进度百分比
    const progressPercentage = computed(() => {
      if (totalTime.value === 0) return 0;
      return (currentTime.value / totalTime.value) * 100;
    });
    // 音量
    const volume = ref(80);
    // 是否静音
    const isMuted = ref(false);
    // 播放模式：0-顺序播放，1-单曲循环，2-随机播放
    const playMode = ref(0);
    // 播放列表
    const playlist = ref([]);
    // 当前播放索引
    const currentIndex = ref(-1);
    // 当前播放的音频元素
    // 音频元素
    const audioElement = ref(new Audio());

    // 当前播放歌曲
    const parsedLrc = ref(null);
    // 完整歌词数组
    const fullLyric = ref([]);
    // 当前歌词行索引
    const currentLyricIndex = ref(-1);

    // 格式化时间
    const formatTime = seconds => {
      if (isNaN(seconds)) return '00:00';
      const minutes = Math.floor(seconds / 60);
      const remainingSeconds = Math.floor(seconds % 60);
      return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
    };

    // 设置音频元素
    const setAudioElement = element => {
      audioElement.value = element;
    };

    // 设置音量
    const setVolume = newVolume => {
      volume.value = newVolume;
      if (audioElement.value) {
        audioElement.value.volume = newVolume / 100;
        if (newVolume > 0) {
          isMuted.value = false;
          audioElement.value.muted = false;
        }
      }
    };

    // 切换静音
    const toggleMute = () => {
      isMuted.value = !isMuted.value;
      if (audioElement.value) {
        audioElement.value.muted = isMuted.value;
      }
    };

    // 设置播放模式
    const setPlayMode = mode => {
      playMode.value = mode;
    };

    // 添加到播放列表
    const addToPlaylist = song => {
      const existingIndex = playlist.value.findIndex(item => item.id === song.id);
      if (existingIndex === -1) {
        playlist.value.push(song);
      }
    };

    // 批量添加歌曲到播放列表并通过id去重
    const addSongsToPlaylist = songs => {
      if (!Array.isArray(songs)) return;

      // 先过滤输入数组中的重复歌曲
      const uniqueSongs = Array.from(new Map(songs.map(song => [song.id, song])).values());

      // 再检查播放列表中是否已存在，不存在则添加
      uniqueSongs.forEach(song => {
        const existingIndex = playlist.value.findIndex(item => item.id === song.id);
        if (existingIndex === -1) {
          playlist.value.push(song);
        }
      });
    };

    // 设置播放列表
    const setPlaylist = (songs, playIndex = 0) => {
      playlist.value = songs;
      currentIndex.value = playIndex;
      if (playIndex >= 0 && playIndex < songs.length) {
        currentSong.value = songs[playIndex];
        playSong(songs[playIndex]);
      }
    };

    // 播放歌曲
    const playSong = async song => {
      if (!song) return;

      currentSong.value = song;
      audioElement.value.src = song.url;
      audioElement.value.load();
      audioElement.value
        .play()
        .then(() => {
          isPlaying.value = true;
        })
        .catch(err => {
          console.error('播放失败:', err);
        });
      // 添加timeupdate事件监听以更新播放时间
      audioElement.value.addEventListener('timeupdate', () => {
        updateCurrentTime(audioElement.value.currentTime);
      });
      // 更新页面标题为当前歌曲名
      document.title = `${song.title} - 音乐播放器`;
      const existingIndex = playlist.value.findIndex(item => item.id === song.id);
      if (existingIndex !== -1) {
        currentIndex.value = existingIndex;
      } else {
        playlist.value.push(song);
        currentIndex.value = playlist.value.length - 1;
      }

      try {
        // 获取当前音质设置
        const currentQuality = 'standard'; // 这里可以从用户设置中获取，暂时使用默认值

        // 获取音乐资源 - 传入歌曲ID和音质参数
        const result = await api.getMediaSource(song, currentQuality);

        if (result.url && audioElement.value) {
          audioElement.value.src = result.url;

          // 设置请求头（如果有）
          if (result.headers) {
            // 注意：浏览器的audio元素不支持直接设置请求头
            // 在实际项目中，可能需要使用fetch API获取音频流，然后创建blob URL
            console.log('音频请求头:', result.headers);
          }

          // 开始播放
          audioElement.value.play();
          isPlaying.value = true;
        }
      } catch (error) {
        console.error('播放失败:', error);
      }
    };

    // 切换播放状态
    const togglePlay = () => {
      if (!audioElement.value) return;

      if (isPlaying.value) {
        audioElement.value.pause();
      } else {
        audioElement.value.play();
      }
      isPlaying.value = !isPlaying.value;
    };

    // 上一首
    const previousSong = () => {
      if (playlist.value.length === 0) return;

      if (playMode.value === 2) {
        // 随机播放
        currentIndex.value = Math.floor(Math.random() * playlist.value.length);
      } else {
        currentIndex.value =
          (currentIndex.value - 1 + playlist.value.length) % playlist.value.length;
      }

      playSong(playlist.value[currentIndex.value]);
    };

    // 下一首
    const nextSong = () => {
      if (playlist.value.length === 0) return;

      if (playMode.value === 2) {
        // 随机播放
        currentIndex.value = Math.floor(Math.random() * playlist.value.length);
      } else {
        currentIndex.value = (currentIndex.value + 1) % playlist.value.length;
      }

      playSong(playlist.value[currentIndex.value]);
    };

    // 设置播放进度
    const setProgress = percentage => {
      if (!audioElement.value || totalTime.value === 0) return;

      const newTime = (percentage / 100) * totalTime.value;
      audioElement.value.currentTime = newTime;
      currentTime.value = newTime;
    };

    // 更新当前播放时间
    const updateCurrentTime = time => {
      currentTime.value = time;
    };

    // 添加currentTime监听以更新歌词索引
    watch(
      () => currentTime.value,
      newTime => {
        updateCurrentLyric(newTime);
      }
    );

    // 更新总时长
    const updateTotalTime = time => {
      totalTime.value = time;
    };

    // 播放完成
    const onEnded = () => {
      if (playMode.value === 1) {
        // 单曲循环
        audioElement.value.currentTime = 0;
        audioElement.value.play();
      } else {
        nextSong();
      }
    };

    const removeSong = (song, index) => {
      if (index === currentIndex.value) {
        // 如果是当前播放歌曲，切换到下一首
        nextSong();
      }
      playlist.value.splice(index, 1);
    };

    // 解析歌词
    const parseLyric = lrcText => {
      if (!lrcText) {
        fullLyric.value = [];
        parsedLrc.value = null;
        currentLyricIndex.value = -1;
        return;
      }

      const lines = lrcText.split('\n');
      const lyricArray = [];

      const timeRegex = /\[(\d{2}):(\d{2})\.(\d{2,3})\]/g;

      lines.forEach(line => {
        let match;
        const times = [];

        // 提取所有时间戳
        while ((match = timeRegex.exec(line)) !== null) {
          const minutes = parseInt(match[1]);
          const seconds = parseInt(match[2]);
          const milliseconds = parseInt(match[3].padEnd(3, '0'));
          const totalSeconds = minutes * 60 + seconds + milliseconds / 1000;
          times.push(totalSeconds);
        }

        // 提取歌词文本
        const text = line.replace(timeRegex, '').trim();

        // 如果有时间戳和歌词文本，添加到数组
        if (times.length > 0 && text) {
          times.forEach(time => {
            lyricArray.push({ time, lrc: text });
          });
        }
      });

      // 按时间排序
      lyricArray.sort((a, b) => a.time - b.time);
      fullLyric.value = lyricArray;
      currentLyricIndex.value = -1;
    };

    // 更新当前歌词
    const updateCurrentLyric = currentTime => {
      if (!fullLyric.value.length) {
        parsedLrc.value = null;
        currentLyricIndex.value = -1;
        return;
      }

      let index = 0;
      while (index < fullLyric.value.length && fullLyric.value[index].time <= currentTime) {
        index++;
      }

      // 当前歌词是最后一个满足条件的索引
      const newIndex = Math.max(0, index - 1);
      if (newIndex !== currentLyricIndex.value) {
        currentLyricIndex.value = newIndex;
        parsedLrc.value = fullLyric.value[newIndex];
      }
    };

    // 设置歌词
    const setLyric = lyric => {
      parseLyric(lyric);
    };

    return {
      // 状态
      currentSong,
      isPlaying,
      currentTime,
      totalTime,
      progressPercentage,
      volume,
      isMuted,
      playMode,
      playlist,
      currentIndex,
      audioElement,
      parsedLrc,
      fullLyric,
      currentLyricIndex,
      addSongsToPlaylist,
      // 计算属性
      formatTime,

      // 方法
      setAudioElement,
      setVolume,
      toggleMute,
      setPlayMode,
      addToPlaylist,
      setPlaylist,
      playSong,
      togglePlay,
      previousSong,
      nextSong,
      setProgress,
      updateCurrentTime,
      updateTotalTime,
      onEnded,
      removeSong,
      parseLyric,
      updateCurrentLyric,
      setLyric,
    };
  },
  {
    persist: {
      storage: localStorage,
    },
  }
);
