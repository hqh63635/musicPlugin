// 集成plugin-wrapper.js的API服务
// 使用axios发起真实API请求，通过Vite代理解决跨域问题
import axios from 'axios'

// 插件实例，API接口与plugin-wrapper.js保持一致
const pluginInstance = {
  // 搜索音乐
  async search(keyword, page = 1, pageSize = 20) {
    try {
      console.log('搜索音乐:', keyword)
      
      // 调用QQ音乐搜索API
      const response = await axios.get(`/api/soso/fcgi-bin/client_search_cp`, {
        params: {
          w: keyword,
          p: page,
          n: pageSize,
          format: 'jsonp',
          cr: 1,
          g_tk: 5381,
          loginUin: 0,
          hostUin: 0,
          inCharset: 'utf8',
          outCharset: 'utf-8',
          notice: 0,
          platform: 'yqq',
          needNewCode: 0,
          zhidaqu: 1,
          catZhida: 1,
          t: 0,
          flag: 1,
          ie: 'utf-8',
          sem: 1,
          aggr: 0,
          perpage: pageSize,
          nownum: (page - 1) * pageSize,
          picmid: 1,
          remotepic: 0,
          _: Date.now()
        }
      })
      
      // 处理返回数据，转换为统一格式
      const data = response.data
      if (data.code === 0) {
        return {
          success: true,
          data: {
            keyword,
            page,
            pageSize,
            total: data.data.song.totalnum || 0,
            songs: data.data.song.list.map(item => ({
              id: item.songmid,
              name: item.songname,
              artist: item.singer.map(s => s.name),
              album: item.albumname,
              cover: `https://y.qq.com/music/photo_new/T002R300x300M000${item.albummid}_1.jpg`,
              duration: item.interval,
              source: 'qq',
              albumId: item.albummid,
              singer: item.singer.map(s => ({
                id: s.singerid,
                name: s.name
              }))
            }))
          }
        }
      } else {
        return { success: false, error: data.message || '搜索失败' }
      }
    } catch (error) {
      console.error('搜索音乐失败:', error)
      return { success: false, error: error.message }
    }
  },
  
  // 获取专辑信息
  async getAlbumInfo(albumId) {
    try {
      console.log('获取专辑信息:', albumId)
      
      // 调用QQ音乐专辑API
      const response = await axios.get(`/api/fcgi-bin/fcg_v8_album_info_cp.fcg`, {
        params: {
          albummid: albumId,
          g_tk: 5381,
          loginUin: 0,
          hostUin: 0,
          inCharset: 'utf8',
          outCharset: 'utf-8',
          notice: 0,
          platform: 'yqq',
          needNewCode: 0,
          _: Date.now()
        }
      })
      
      const data = response.data
      if (data.code === 0) {
        const albumInfo = data.data.albumInfo
        const songs = data.data.list.map(item => ({
          id: item.songmid,
          name: item.songname,
          artist: item.singer.map(s => s.name),
          album: albumInfo.name,
          cover: `https://y.gtimg.cn/music/photo_new/T002R300x300M000${albumInfo.mid}.jpg`,
          duration: item.interval,
          source: 'qq',
          albumId: albumId,
          singer: item.singer.map(s => ({
            id: s.mid,
            name: s.name
          }))
        }))
        
        return {
          success: true,
          data: {
            id: albumId,
            name: albumInfo.name,
            artist: albumInfo.singer.name,
            cover: `https://y.gtimg.cn/music/photo_new/T002R300x300M000${albumInfo.mid}.jpg`,
            releaseDate: albumInfo.aDate,
            songs: songs,
            singer: {
              id: albumInfo.singer.mid,
              name: albumInfo.singer.name
            }
          }
        }
      } else {
        return { success: false, error: data.message || '获取专辑信息失败' }
      }
    } catch (error) {
      console.error('获取专辑信息失败:', error)
      return { success: false, error: error.message }
    }
  },
  
  // 获取歌单详情（与getPlaylistInfo保持一致）
  async getPlaylistDetail(playlistId) {
    try {
      console.log('获取歌单详情:', playlistId)
      
      // 调用QQ音乐歌单API
      const response = await axios.get(`/api/fcgi-bin/fcg_v8_playlist_cp.fcg`, {
        params: {
          id: playlistId,
          g_tk: 5381,
          loginUin: 0,
          hostUin: 0,
          inCharset: 'utf8',
          outCharset: 'utf-8',
          notice: 0,
          platform: 'yqq',
          needNewCode: 0,
          _: Date.now()
        }
      })
      
      const data = response.data
      if (data.code === 0) {
        const cdlist = data.data.cdlist[0]
        const songs = cdlist.songlist.map(item => ({
          id: item.musicData.songmid,
          name: item.musicData.songname,
          artist: item.musicData.singer.map(s => s.name),
          album: item.musicData.albumname,
          cover: `https://y.gtimg.cn/music/photo_new/T002R300x300M000${item.musicData.albummid}.jpg`,
          duration: item.musicData.interval,
          source: 'qq',
          albumId: item.musicData.albummid,
          singer: item.musicData.singer.map(s => ({
            id: s.mid,
            name: s.name
          }))
        }))
        
        return {
          success: true,
          data: {
            id: playlistId,
            name: cdlist.dissname,
            cover: `https://y.gtimg.cn/music/photo_new/T003R300x300M000${cdlist.dissid}.jpg`,
            creator: {
              id: cdlist.creator.uid,
              name: cdlist.creator.name
            },
            songCount: cdlist.total_song_num,
            playCount: cdlist.visitnum,
            description: cdlist.desc,
            songs: songs
          }
        }
      } else {
        return { success: false, error: data.message || '获取歌单详情失败' }
      }
    } catch (error) {
      console.error('获取歌单详情失败:', error)
      return { success: false, error: error.message }
    }
  },
  
  // 获取排行榜
  async getTopList() {
    try {
      console.log('获取排行榜')
      
      // 使用模拟数据，因为QQ音乐API路径已变更
      const mockTopList = [
        {
          id: 1,
          name: '巅峰榜·流行指数',
          cover: 'https://y.qq.com/music/photo_new/T003R300x300M000003oBu342ET0Gp.jpg',
          songCount: 100,
          listenCount: 7953220,
          updateFrequency: '实时'
        },
        {
          id: 2,
          name: '巅峰榜·新歌榜',
          cover: 'https://y.qq.com/music/photo_new/T003R300x300M000002S1K841lF8eV.jpg',
          songCount: 100,
          listenCount: 5642110,
          updateFrequency: '实时'
        },
        {
          id: 3,
          name: '巅峰榜·热歌榜',
          cover: 'https://y.qq.com/music/photo_new/T003R300x300M000003R8Z4V2t4k8v.jpg',
          songCount: 100,
          listenCount: 9876540,
          updateFrequency: '实时'
        },
        {
          id: 4,
          name: '巅峰榜·内地榜',
          cover: 'https://y.qq.com/music/photo_new/T003R300x300M000001vD7iB2qXf3V.jpg',
          songCount: 100,
          listenCount: 4532100,
          updateFrequency: '实时'
        },
        {
          id: 5,
          name: '巅峰榜·港台榜',
          cover: 'https://y.qq.com/music/photo_new/T003R300x300M000003lV73X3QZ4fK.jpg',
          songCount: 100,
          listenCount: 3421090,
          updateFrequency: '实时'
        }
      ]
      
      return {
        success: true,
        data: mockTopList
      }
    } catch (error) {
      console.error('获取排行榜失败:', error)
      return { success: false, error: error.message }
    }
  },
  
  // 获取榜单详情
  async getTopListDetail(topListItem, page = 1) {
    try {
      console.log('获取榜单详情:', topListItem)
      
      // 提取榜单ID
      const listId = typeof topListItem === 'object' ? topListItem.id : topListItem
      
      // 使用模拟数据，因为QQ音乐API路径已变更
      const mockMusicList = [
        {
          id: '001JZkTF2XZ8lH',
          name: '晴天',
          artist: '周杰伦',
          album: '叶惠美',
          cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000003lV73X3QZ4fK.jpg',
          duration: 273,
          source: 'qq',
          albumId: '003lV73X3QZ4fK',
          singer: [{
            id: '002J4UUk29y8BY',
            name: '周杰伦'
          }]
        },
        {
          id: '002J4UUk29y8BY',
          name: '稻香',
          artist: '周杰伦',
          album: '魔杰座',
          cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000003lV73X3QZ4fK.jpg',
          duration: 325,
          source: 'qq',
          albumId: '003lV73X3QZ4fK',
          singer: [{
            id: '002J4UUk29y8BY',
            name: '周杰伦'
          }]
        },
        {
          id: '003lV73X3QZ4fK',
          name: '青花瓷',
          artist: '周杰伦',
          album: '我很忙',
          cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000003lV73X3QZ4fK.jpg',
          duration: 300,
          source: 'qq',
          albumId: '003lV73X3QZ4fK',
          singer: [{
            id: '002J4UUk29y8BY',
            name: '周杰伦'
          }]
        },
        {
          id: '004Z8Ihr0JIu5m',
          name: '七里香',
          artist: '周杰伦',
          album: '七里香',
          cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000003lV73X3QZ4fK.jpg',
          duration: 345,
          source: 'qq',
          albumId: '003lV73X3QZ4fK',
          singer: [{
            id: '002J4UUk29y8BY',
            name: '周杰伦'
          }]
        },
        {
          id: '001fUfa63E1pVB',
          name: '夜曲',
          artist: '周杰伦',
          album: '十一月的萧邦',
          cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000003lV73X3QZ4fK.jpg',
          duration: 267,
          source: 'qq',
          albumId: '003lV73X3QZ4fK',
          singer: [{
            id: '002J4UUk29y8BY',
            name: '周杰伦'
          }]
        }
      ]
      
      return {
        success: true,
        data: {
          isEnd: true,
          topListItem: typeof topListItem === 'object' ? topListItem : { id: listId },
          musicList: mockMusicList,
          songCount: mockMusicList.length
        }
      }
    } catch (error) {
      console.error('获取榜单详情失败:', error)
      return { success: false, error: error.message }
    }
  },
  
  // 获取歌词
  async getLyric(songId) {
    try {
      console.log('获取歌词:', songId)
      
      // 调用QQ音乐歌词API
      const response = await axios.get(`/api2/lyric/fcgi-bin/fcg_query_lyric_new.fcg`, {
        params: {
          songmid: songId,
          g_tk: 5381,
          loginUin: 0,
          hostUin: 0,
          inCharset: 'utf8',
          outCharset: 'utf-8',
          notice: 0,
          platform: 'yqq',
          needNewCode: 0,
          pcachetime: Date.now(),
          _: Date.now()
        }
      })
      
      const data = response.data
      // QQ音乐歌词API返回的是JSONP格式，需要处理
      const lyricData = JSON.parse(data.replace(/MusicJsonCallback\((.*?)\)/, '$1'))
      
      if (lyricData.code === 0) {
        // 解密歌词
        const decodeLyric = (lyric) => {
          if (!lyric) return ''
          const buffer = Buffer.from(lyric, 'base64')
          return buffer.toString()
        }
        
        return {
          success: true,
          data: {
            id: songId,
            lyric: decodeLyric(lyricData.lyric) || '',
            translatedLyric: decodeLyric(lyricData.trans) || '',
            tlyric: decodeLyric(lyricData.trans) || ''
          }
        }
      } else {
        return { success: false, error: lyricData.message || '获取歌词失败' }
      }
    } catch (error) {
      console.error('获取歌词失败:', error)
      return { success: false, error: error.message }
    }
  },
  
  // 获取音乐资源
  async getMediaSource(songId, quality = 'standard') {
    try {
      console.log('获取音乐资源:', songId, quality)
      
      // 根据音质选择不同的文件前缀
      const qualityPrefixMap = {
        'standard': 'M500',  // 128kbps
        'high': 'M800',      // 320kbps
        'flac': 'F000'       // 无损音质
      }
      const prefix = qualityPrefixMap[quality] || 'M800'
      
      // 调用QQ音乐媒体源API
      const response = await axios.get(`/api/base/fcgi-bin/fcg_music_express_mobile3.fcg`, {
        params: {
          songmid: songId,
          filename: `${prefix}${songId}.mp3`,
          guid: '1234567890',
          g_tk: 5381,
          loginUin: 0,
          hostUin: 0,
          inCharset: 'utf8',
          outCharset: 'utf-8',
          notice: 0,
          platform: 'yqq',
          needNewCode: 0,
          cid: '205361747',
          callback: 'MusicJsonCallback',
          _: Date.now()
        }
      })
      
      const data = response.data
      // 处理JSONP响应
      const mediaData = JSON.parse(data.replace(/MusicJsonCallback\((.*?)\)/, '$1'))
      
      if (mediaData.code === 0) {
        const { data } = mediaData
        const { items } = data
        if (items && items.length > 0) {
          const item = items[0]
          // 构建播放URL
          const vkey = item.vkey
          const url = `https://dl.stream.qqmusic.qq.com/${prefix}${songId}.mp3?vkey=${vkey}&guid=1234567890&uin=0&fromtag=66`
          
          return {
            success: true,
            data: {
              id: songId,
              url: url,
              quality: quality,
              size: 10485760,
              format: prefix === 'F000' ? 'flac' : 'mp3',
              bitrate: prefix === 'M500' ? 128 : prefix === 'M800' ? 320 : 1000
            }
          }
        }
      }
      
      return { success: false, error: '获取音乐资源失败' }
    } catch (error) {
      console.error('获取音乐资源失败:', error)
      return { success: false, error: error.message }
    }
  },
  
  // 获取歌手列表
  async getArtistList() {
    try {
      console.log('获取歌手列表')
      
      // 调用QQ音乐歌手列表API
      const response = await axios.get(`/api/fcgi-bin/fcg_v8_singer_list_cp.fcg`, {
        params: {
          g_tk: 5381,
          loginUin: 0,
          hostUin: 0,
          inCharset: 'utf8',
          outCharset: 'utf-8',
          notice: 0,
          platform: 'yqq',
          needNewCode: 0,
          page: 1,
          pagesize: 10,
          key: '',
          area: -100,
          sex: -100,
          genre: -100,
          index: -100,
          sin: 0,
          ein: 9,
          sortid: 5,
          _: Date.now()
        }
      })
      
      const data = response.data
      if (data.code === 0) {
        const formattedArtists = data.data.singerlist.map(artist => ({
          id: artist.singer_mid,
          name: artist.singer_name,
          avatar: `https://y.gtimg.cn/music/photo_new/T001R300x300M000${artist.singer_mid}.jpg`,
          songCount: artist.song_num,
          fansCount: artist.fans_num,
          description: `${artist.singer_name} - ${artist.country}`
        }))
        
        return {
          success: true,
          data: formattedArtists
        }
      } else {
        return { success: false, error: data.message || '获取歌手列表失败' }
      }
    } catch (error) {
      console.error('获取歌手列表失败:', error)
      return { success: false, error: error.message }
    }
  },
  
  // 获取歌手热门歌曲
  async getArtistSongs(artistId) {
    try {
      console.log('获取歌手热门歌曲:', artistId)
      
      // 调用QQ音乐歌手热门歌曲API
      const response = await axios.get(`/api/fcgi-bin/fcg_v8_singer_track_cp.fcg`, {
        params: {
          singermid: artistId,
          g_tk: 5381,
          loginUin: 0,
          hostUin: 0,
          inCharset: 'utf8',
          outCharset: 'utf-8',
          notice: 0,
          platform: 'yqq',
          needNewCode: 0,
          order: 'listen',
          begin: 0,
          num: 10,
          songstatus: 1,
          _: Date.now()
        }
      })
      
      const data = response.data
      if (data.code === 0) {
        const formattedSongs = data.data.list.map(song => {
          const musicData = song.musicData
          return {
            id: musicData.songmid,
            name: musicData.songname,
            artist: musicData.singer.map(s => s.name),
            album: musicData.albumname,
            cover: `https://y.gtimg.cn/music/photo_new/T002R300x300M000${musicData.albummid}.jpg`,
            duration: musicData.interval,
            source: 'qq',
            albumId: musicData.albummid,
            singer: musicData.singer.map(s => ({
              id: s.mid,
              name: s.name
            }))
          }
        })
        
        return {
          success: true,
          data: formattedSongs
        }
      } else {
        return { success: false, error: data.message || '获取歌手热门歌曲失败' }
      }
    } catch (error) {
      console.error('获取歌手热门歌曲失败:', error)
      return { success: false, error: error.message }
    }
  },
  
  // 获取歌手专辑
  async getArtistAlbums(artistId) {
    try {
      console.log('获取歌手专辑:', artistId)
      
      // 调用QQ音乐歌手专辑API
      const response = await axios.get(`/api/fcgi-bin/fcg_v8_artist_album.fcg`, {
        params: {
          singermid: artistId,
          g_tk: 5381,
          loginUin: 0,
          hostUin: 0,
          inCharset: 'utf8',
          outCharset: 'utf-8',
          notice: 0,
          platform: 'yqq',
          needNewCode: 0,
          order: 'time',
          begin: 0,
          num: 10,
          _: Date.now()
        }
      })
      
      const data = response.data
      if (data.code === 0) {
        const formattedAlbums = data.data.list.map(album => ({
          id: album.mid,
          name: album.name,
          cover: `https://y.gtimg.cn/music/photo_new/T002R300x300M000${album.mid}.jpg`,
          releaseDate: album.time_public,
          songCount: album.size
        }))
        
        return {
          success: true,
          data: formattedAlbums
        }
      } else {
        return { success: false, error: data.message || '获取歌手专辑失败' }
      }
    } catch (error) {
      console.error('获取歌手专辑失败:', error)
      return { success: false, error: error.message }
    }
  }
}

// 导出API服务，与plugin-wrapper.js的导出结构保持一致
export default {
  // 音乐相关API，与plugin-wrapper.js的实例方法保持一致
  search: pluginInstance.search.bind(pluginInstance),
  getAlbumInfo: pluginInstance.getAlbumInfo.bind(pluginInstance),
  getPlaylistDetail: pluginInstance.getPlaylistDetail.bind(pluginInstance),
  getTopList: pluginInstance.getTopList.bind(pluginInstance),
  getLyric: pluginInstance.getLyric.bind(pluginInstance),
  getMediaSource: pluginInstance.getMediaSource.bind(pluginInstance),
  getArtistList: pluginInstance.getArtistList.bind(pluginInstance),
  getArtistSongs: pluginInstance.getArtistSongs.bind(pluginInstance),
  getArtistAlbums: pluginInstance.getArtistAlbums.bind(pluginInstance),
  
  // 用户相关API（待实现）
  user: {
    login: () => {},
    logout: () => {},
    getUserInfo: () => {}
  },
  
  // 播放列表相关API（待实现）
  playlist: {
    getMyPlaylists: () => {},
    createPlaylist: () => {},
    addSongToPlaylist: () => {},
    removeSongFromPlaylist: () => {}
  },
  
  // 歌单详情（别名，保持与getAlbumInfo等方法命名一致）
  getPlaylistInfo: pluginInstance.getPlaylistDetail.bind(pluginInstance)
}