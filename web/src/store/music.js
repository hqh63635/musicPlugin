import { defineStore } from 'pinia';
import { ref, computed, watch } from 'vue';
import api from '@/services/api.js';
import { sameSong, updateSongUrl } from '@/composables/useMusicSheetsDB.js';
import { message } from 'ant-design-vue';

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
    // 音频元素
    const audioElement = ref(new Audio());
    // 音质设置
    const quality = ref('exhigh'); // low, standard, high, super
    // 当前播放歌曲
    const parsedLrc = ref(null);
    // 完整歌词数组
    const fullLyric = ref([]);
    // 当前歌词行索引
    const currentLyricIndex = ref(-1);
    // API源设置
    const apiSource = ref('changqing');
    const supportedApiSources = new Set(['changqing', 'cihedai']);
    if (!supportedApiSources.has(apiSource.value)) {
      apiSource.value = 'changqing';
    }

    // 歌词抽屉状态
    const showLyricDrawer = ref(true);

    // 收藏歌单
    const favoriteSheets = ref([]);

    // 歌单列表
    const musicSheets = ref([]);

    // 当前查看的歌单
    const currentSheet = ref(null);

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
        playlist.value.push({
          ...song,
          playTime: new Date().toISOString(),
          isFavorite: false,
        });
      } else {
        // 更新已存在歌曲的播放时间
        playlist.value[existingIndex].playTime = new Date().toISOString();
      }
    };

    // 批量添加歌曲到播放列表并通过id去重
    const addSongsToPlaylist = songs => {
      if (!Array.isArray(songs)) return;

      // 先过滤输入数组中的重复歌曲
      const uniqueSongs = Array.from(
        new Map(
          songs.map(song => [
            song.id,
            {
              ...song,
              playTime: new Date().toISOString(),
              isFavorite: false,
            },
          ])
        ).values()
      );

      // 再检查播放列表中是否已存在，不存在则添加
      uniqueSongs.forEach(song => {
        const existingIndex = playlist.value.findIndex(item => item.id === song.id);
        if (existingIndex === -1) {
          playlist.value.push(song);
        } else {
          // 更新已存在歌曲的播放时间
          playlist.value[existingIndex].playTime = new Date().toISOString();
        }
      });
    };

    // 设置播放列表
    const setPlaylist = (songs, playIndex = 0) => {
      // 为每个歌曲添加playTime和isFavorite属性
      const songsWithMeta = songs.map(song => ({
        ...song,
        playTime: new Date().toISOString(),
        isFavorite: false,
      }));
      playlist.value = songsWithMeta;
      currentIndex.value = playIndex;
      if (playIndex >= 0 && playIndex < songsWithMeta.length) {
        currentSong.value = songsWithMeta[playIndex];
        playSong(songsWithMeta[playIndex]);
      }
    };

    let playbackGeneration = 0;
    let retryUsed = false;
    let retryPending = false;

    const clearSongUrl = async song => {
      const empty = { url: '', urlSource: '', urlQuality: '' };
      Object.assign(song, empty);
      for (const item of playlist.value) if (sameSong(item, song)) Object.assign(item, empty);
      for (const sheet of musicSheets.value) {
        for (const item of sheet.musicList || []) if (sameSong(item, song)) Object.assign(item, empty);
      }
      if (currentSheet.value) {
        for (const item of currentSheet.value.musicList || []) if (sameSong(item, song)) Object.assign(item, empty);
      }
      if (currentSong.value && sameSong(currentSong.value, song)) Object.assign(currentSong.value, empty);
      await updateSongUrl(song, empty);
    };

    const resetSongUrl = async song => {
      if (currentSong.value && sameSong(currentSong.value, song)) {
        ++playbackGeneration;
        audioElement.value.pause();
        audioElement.value.removeAttribute('src');
        audioElement.value.load();
        isPlaying.value = false;
      }
      await clearSongUrl(song);
    };

    const loadMedia = async (song, force = false) => {
      const generation = ++playbackGeneration;
      const result = await api.getMediaSource(song, quality.value, apiSource.value, force);
      if (generation !== playbackGeneration || currentSong.value !== song) return;
      if (!result?.url) throw new Error('未获取到播放地址');
      if (/\.(?:mgg|mgg\d*|qmc\w*|mflac\w*)(?:[?#]|$)/i.test(result.url)) {
        await clearSongUrl(song);
        message.error('音源返回加密音频，已清除地址，请切换音源或音质');
        throw new Error('音源返回加密音频，已清除地址，请切换音源或音质');
      }
      for (const item of playlist.value) {
        if (sameSong(item, song)) {
          Object.assign(item, {
            url: song.url,
            urlSource: song.urlSource,
            urlQuality: song.urlQuality,
          });
        }
      }
      audioElement.value.src = result.url;
      audioElement.value.load();
      try {
        await audioElement.value.play();
        if (generation === playbackGeneration) isPlaying.value = true;
      } catch (error) {
        if (error.name !== 'AbortError' && error.name !== 'NotAllowedError') {
          if (generation === playbackGeneration) await clearSongUrl(song);
          throw error;
        }
      }
    };

    audioElement.value.addEventListener('error', async () => {
      if (!currentSong.value || retryPending) return;
      isPlaying.value = false;
      if (retryUsed) {
        await clearSongUrl(currentSong.value);
        message.error('音源无法播放，请切换音源或音质');
        return;
      }
      retryUsed = true;
      retryPending = true;
      const generation = playbackGeneration;
      const song = currentSong.value;
      try {
        await clearSongUrl(song);
        if (generation === playbackGeneration) await loadMedia(song, true);
      }
      catch (error) { console.error('重新获取音源失败:', error); }
      finally { if (playbackGeneration === generation + 1) retryPending = false; }
    });

    const playSong = async song => {
      if (!song) return;
      ++playbackGeneration;
      audioElement.value.pause();
      audioElement.value.removeAttribute('src');
      audioElement.value.load();
      isPlaying.value = false;
      retryUsed = false;
      retryPending = false;
      setLyric('');
      currentLyricIndex.value = -1;
      const cached = playlist.value.find(item => sameSong(item, song)
        && item.url && item.urlSource === apiSource.value && item.urlQuality === quality.value);
      if (cached) Object.assign(song, {
        url: cached.url, urlSource: cached.urlSource, urlQuality: cached.urlQuality,
      });
      currentSong.value = song;
      song = currentSong.value;
      song.playTime = new Date().toISOString();
      document.title = song.title + ' - ' + song.artist;
      addToPlaylist(song);
      currentIndex.value = playlist.value.findIndex(item => item.id === song.id);
      api.getLyric(song).then(result => {
        if (currentSong.value === song) setLyric(result?.lyric || '');
      }).catch(error => console.error('获取歌词失败:', error));
      try { await loadMedia(song); }
      catch (error) { console.error('播放失败:', error); }
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

    // 监听currentTime变化以更新歌词索引
    watch(
      () => currentTime.value,
      newTime => {
        updateCurrentLyric(newTime);
      }
    );

    watch([quality, apiSource], async ([newQuality, newSource], [oldQuality, oldSource]) => {
      if (!supportedApiSources.has(newSource)) { apiSource.value = 'plugin'; return; }
      if (!currentSong.value || (newQuality === oldQuality && newSource === oldSource)) return;
      const song = currentSong.value;
      const position = audioElement.value.currentTime;
      const wasPlaying = isPlaying.value;
      retryUsed = false;
      retryPending = false;
      try {
        await loadMedia(song);
        if (currentSong.value === song) {
          audioElement.value.currentTime = position;
          if (!wasPlaying) { audioElement.value.pause(); isPlaying.value = false; }
        }
      } catch (error) { console.error('切换音源或音质失败:', error); }
    });

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

    // 切换歌词抽屉状态
    const toggleLyricDrawer = () => {
      showLyricDrawer.value = !showLyricDrawer.value;
    };

    // 切换歌单收藏状态
    const toggleFavoriteSheet = sheet => {
      if (!sheet) return;

      const index = favoriteSheets.value.findIndex(
        item => item.platform === sheet.platform && item.id === sheet.id
      );

      if (index === -1) {
        // 收藏歌单
        favoriteSheets.value.push(sheet);
      } else {
        // 取消收藏
        favoriteSheets.value.splice(index, 1);
      }
    };

    // 检查歌单是否已收藏
    const isSheetFavorite = (platform, id) => {
      return favoriteSheets.value.some(item => item.platform === platform && item.id === id);
    };

    // 设置歌单列表
    const setMusicSheets = sheets => {
      musicSheets.value = sheets;
    };

    // 添加歌单
    const addMusicSheet = sheet => {
      musicSheets.value.push(sheet);
    };

    // 更新歌单
    const updateMusicSheet = updatedSheet => {
      const index = musicSheets.value.findIndex(sheet => sheet.id === updatedSheet.id);
      if (index !== -1) {
        musicSheets.value[index] = updatedSheet;
      }
    };

    // 删除歌单
    const deleteMusicSheet = sheetId => {
      musicSheets.value = musicSheets.value.filter(sheet => sheet.id !== sheetId);
      // 如果删除的是当前歌单，清除当前歌单
      if (currentSheet.value && currentSheet.value.id === sheetId) {
        currentSheet.value = null;
      }
    };

    // 设置当前歌单
    const setCurrentSheet = sheet => {
      currentSheet.value = sheet;
    };

    // 清空歌单歌曲
    const clearSheetSongs = sheetId => {
      const sheet = musicSheets.value.find(sheet => sheet.id === sheetId);
      if (sheet) {
        sheet.musicList = [];
        sheet.trackCount = 0;
        updateMusicSheet(sheet);
        // 如果是当前歌单，同步更新当前歌单
        if (currentSheet.value && currentSheet.value.id === sheetId) {
          currentSheet.value = { ...sheet };
        }
      }
    };

    // 设置音质
    const setQuality = newQuality => {
      quality.value = newQuality;
    };

    // 设置API源
    const setApiSource = newSource => {
      apiSource.value = newSource;
    };

    // 清空播放列表
    const removeAll = () => {
      playlist.value = [];
      currentIndex.value = -1;
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
      showLyricDrawer,
      favoriteSheets,
      musicSheets,
      currentSheet,
      quality,
      apiSource,
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
      resetSongUrl,
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
      toggleLyricDrawer,
      toggleFavoriteSheet,
      isSheetFavorite,
      setMusicSheets,
      addMusicSheet,
      updateMusicSheet,
      deleteMusicSheet,
      setCurrentSheet,
      clearSheetSongs,
      setQuality,
      setApiSource,
      removeAll,
    };
  },
  {
    persist: {
      storage: localStorage,
      pick: [
        'currentSong',
        'isPlaying',
        'progressPercentage',
        'volume',
        'isMuted',
        'playMode',
        'playlist',
        'currentIndex',
        'parsedLrc',
        'fullLyric',
        'currentLyricIndex',
        'showLyricDrawer',
        'favoriteSheets',
        'musicSheets',
        'currentSheet',
        'quality',
        'apiSource',
      ],
    },
  }
);
