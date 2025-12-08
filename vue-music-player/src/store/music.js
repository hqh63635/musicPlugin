import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api.js'

export const useMusicStore = defineStore('music', () => {
  // 当前播放歌曲信息
  const currentSong = ref(null)
  // 播放状态
  const isPlaying = ref(false)
  // 当前播放进度（秒）
  const currentTime = ref(0)
  // 总时长（秒）
  const totalTime = ref(0)
  // 播放进度百分比
  const progressPercentage = computed(() => {
    if (totalTime.value === 0) return 0
    return (currentTime.value / totalTime.value) * 100
  })
  // 音量
  const volume = ref(80)
  // 是否静音
  const isMuted = ref(false)
  // 播放模式：0-顺序播放，1-单曲循环，2-随机播放
  const playMode = ref(0)
  // 播放列表
  const playlist = ref([])
  // 当前播放索引
  const currentIndex = ref(-1)
  // 当前播放的音频元素
  const audioElement = ref(null)
  
  // 格式化时间
  const formatTime = (seconds) => {
    if (isNaN(seconds)) return '00:00'
    const minutes = Math.floor(seconds / 60)
    const remainingSeconds = Math.floor(seconds % 60)
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
  }
  
  // 设置音频元素
  const setAudioElement = (element) => {
    audioElement.value = element
  }
  
  // 设置音量
  const setVolume = (newVolume) => {
    volume.value = newVolume
    if (audioElement.value) {
      audioElement.value.volume = newVolume / 100
      if (newVolume > 0) {
        isMuted.value = false
        audioElement.value.muted = false
      }
    }
  }
  
  // 切换静音
  const toggleMute = () => {
    isMuted.value = !isMuted.value
    if (audioElement.value) {
      audioElement.value.muted = isMuted.value
    }
  }
  
  // 设置播放模式
  const setPlayMode = (mode) => {
    playMode.value = mode
  }
  
  // 添加到播放列表
  const addToPlaylist = (song) => {
    const existingIndex = playlist.value.findIndex(item => item.id === song.id)
    if (existingIndex === -1) {
      playlist.value.push(song)
    }
  }
  
  // 设置播放列表
  const setPlaylist = (songs, playIndex = 0) => {
    playlist.value = songs
    currentIndex.value = playIndex
    if (playIndex >= 0 && playIndex < songs.length) {
      currentSong.value = songs[playIndex]
      playSong(songs[playIndex])
    }
  }
  
  // 播放歌曲
  const playSong = async (song) => {
    if (!song) return
    
    currentSong.value = song
    const existingIndex = playlist.value.findIndex(item => item.id === song.id)
    if (existingIndex !== -1) {
      currentIndex.value = existingIndex
    } else {
      playlist.value.push(song)
      currentIndex.value = playlist.value.length - 1
    }
    
    try {
      // 获取当前音质设置
      const currentQuality = 'standard' // 这里可以从用户设置中获取，暂时使用默认值
      
      // 获取音乐资源 - 传入歌曲ID和音质参数
      const result = await api.getMediaSource(song, currentQuality)
      if (result.success && result.data && audioElement.value) {
        audioElement.value.src = result.data.url
        
        // 设置请求头（如果有）
        if (result.data.headers) {
          // 注意：浏览器的audio元素不支持直接设置请求头
          // 在实际项目中，可能需要使用fetch API获取音频流，然后创建blob URL
          console.log('音频请求头:', result.data.headers)
        }
        
        // 开始播放
        audioElement.value.play()
        isPlaying.value = true
      }
    } catch (error) {
      console.error('播放失败:', error)
    }
  }
  
  // 切换播放状态
  const togglePlay = () => {
    if (!audioElement.value) return
    
    if (isPlaying.value) {
      audioElement.value.pause()
    } else {
      audioElement.value.play()
    }
    isPlaying.value = !isPlaying.value
  }
  
  // 上一首
  const previousSong = () => {
    if (playlist.value.length === 0) return
    
    if (playMode.value === 2) { // 随机播放
      currentIndex.value = Math.floor(Math.random() * playlist.value.length)
    } else {
      currentIndex.value = (currentIndex.value - 1 + playlist.value.length) % playlist.value.length
    }
    
    playSong(playlist.value[currentIndex.value])
  }
  
  // 下一首
  const nextSong = () => {
    if (playlist.value.length === 0) return
    
    if (playMode.value === 2) { // 随机播放
      currentIndex.value = Math.floor(Math.random() * playlist.value.length)
    } else {
      currentIndex.value = (currentIndex.value + 1) % playlist.value.length
    }
    
    playSong(playlist.value[currentIndex.value])
  }
  
  // 设置播放进度
  const setProgress = (percentage) => {
    if (!audioElement.value || totalTime.value === 0) return
    
    const newTime = (percentage / 100) * totalTime.value
    audioElement.value.currentTime = newTime
    currentTime.value = newTime
  }
  
  // 更新当前播放时间
  const updateCurrentTime = (time) => {
    currentTime.value = time
  }
  
  // 更新总时长
  const updateTotalTime = (time) => {
    totalTime.value = time
  }
  
  // 播放完成
  const onEnded = () => {
    if (playMode.value === 1) { // 单曲循环
      audioElement.value.currentTime = 0
      audioElement.value.play()
    } else {
      nextSong()
    }
  }
  
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
    onEnded
  }
})
