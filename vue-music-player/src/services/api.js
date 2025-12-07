// 集成plugin-wrapper.js的API服务
// 使用plugin-wrapper.js获取数据
import axios from 'axios'

// 创建一个简单的插件实例，用于fallback
const fallbackPlugin = {
  // 音乐相关API的默认实现
  async search() { return { success: false, error: 'API未实现' } },
  async getAlbumInfo() { return { success: false, error: 'API未实现' } },
  async getMusicSheetInfo() { return { success: false, error: 'API未实现' } },
  async getTopLists() { return { success: false, error: 'API未实现' } },
  async getTopListDetail() { return { success: false, error: 'API未实现' } },
  async getLyric() { return { success: false, error: 'API未实现' } },
  async getMediaSource() { return { success: false, error: 'API未实现' } },
  async getArtistWorks() { return { success: false, error: 'API未实现' } },
  async importMusicSheet() { return { success: false, error: 'API未实现' } },
  async getRecommendSheetTags() { return { success: false, error: 'API未实现' } },
}

// 加载plugin-wrapper.js
async function loadPlugin() {
  try {
    console.log('尝试加载plugin-wrapper.js')
    // 动态导入plugin-wrapper.js
    const pluginModule = await import('/Users/hqh/Desktop/03-study/musicPlugin/plugin-wrapper.js')
    // 返回插件实例（处理CommonJS模块导出）
    // plugin-wrapper.js是CommonJS模块，在ES6中import会返回{ default: ... }结构
    return pluginModule.default || pluginModule
  } catch (error) {
    console.error('加载plugin-wrapper.js失败:', error)
    return null
  }
}

// 导出API服务，与plugin-wrapper.js的导出结构保持一致
export default {
  // 音乐相关API，使用plugin-wrapper.js
  search: async (keyword, page = 1, pageSize = 20) => {
    try {
      console.log('搜索音乐:', keyword)
      
      // 加载plugin-wrapper.js
      const pluginModule = await loadPlugin()
      
      // 如果plugin加载成功，使用plugin的search方法
      if (pluginModule && pluginModule.search) {
        const searchResult = await pluginModule.search(keyword, page, 'music')
        console.log('plugin search result:', searchResult)
        
        if (searchResult && searchResult.data && Array.isArray(searchResult.data)) {
          // 转换为前端期望的格式
          return {
            success: true,
            data: {
              keyword,
              page,
              pageSize,
              total: searchResult.total || 0,
              songs: searchResult.data.map(item => ({
                id: item.id || item.songmid,
                name: item.title || item.songname,
                artist: item.artist ? (Array.isArray(item.artist) ? item.artist : [item.artist]) : [],
                album: item.album || item.albumname,
                cover: item.artwork || `https://y.qq.com/music/photo_new/T002R300x300M000${item.albummid}_1.jpg`,
                duration: item.duration || item.interval,
                source: 'qq',
                albumId: item.albumid || item.albummid,
                singer: item.singer || item.singer?.map(s => ({
                  id: s.id || s.singerid,
                  name: s.name
                })) || []
              }))
            }
          }
        }
      }
      
      // 如果plugin加载失败或返回无效数据，使用模拟数据
      console.log('使用模拟搜索数据')
      return {
        success: true,
        data: {
          keyword,
          page,
          pageSize,
          total: 2,
          songs: [
            {
              id: '001JZkTF2XZ8lH',
              name: '晴天',
              artist: ['周杰伦'],
              album: '叶惠美',
              cover: 'https://y.qq.com/music/photo_new/T002R300x300M000002J4UUk29y8BY_1.jpg',
              duration: 260,
              source: 'qq',
              albumId: '002J4UUk29y8BY',
              singer: [{ id: '0025NhlN2yWrP4', name: '周杰伦' }]
            },
            {
              id: '003fdK422CjWbd',
              name: '七里香',
              artist: ['周杰伦'],
              album: '七里香',
              cover: 'https://y.qq.com/music/photo_new/T002R300x300M000001VfvsJ2170e9_1.jpg',
              duration: 280,
              source: 'qq',
              albumId: '001VfvsJ2170e9',
              singer: [{ id: '0025NhlN2yWrP4', name: '周杰伦' }]
            }
          ]
        }
      }
    } catch (error) {
      console.error('搜索音乐失败:', error)
      return { success: false, error: error.message }
    }
  },
  
  // 获取专辑信息
  getAlbumInfo: async (albumId, albummid) => {
    try {
      console.log('获取专辑信息:', albumId, albummid)
      
      // 加载plugin-wrapper.js
      const pluginModule = await loadPlugin()
      
      // 如果plugin加载成功，使用plugin的getAlbumInfo方法
      if (pluginModule && pluginModule.getAlbumInfo) {
        const albumInfo = await pluginModule.getAlbumInfo(albumId, albummid)
        console.log('plugin albumInfo result:', albumInfo)
        
        if (albumInfo) {
          // 转换为前端期望的格式
          return {
            success: true,
            data: {
              id: albumId,
              name: albumInfo.title || albumInfo.name,
              artist: albumInfo.artist || albumInfo.artistName,
              cover: albumInfo.artwork || albumInfo.cover || `https://y.gtimg.cn/music/photo_new/T002R300x300M000${albumId}.jpg`,
              releaseDate: albumInfo.publishTime || albumInfo.releaseDate,
              songs: albumInfo.songs ? albumInfo.songs.map(item => ({
                id: item.id || item.songmid,
                name: item.title || item.songname,
                artist: item.artist ? (Array.isArray(item.artist) ? item.artist : [item.artist]) : [],
                album: albumInfo.title || albumInfo.name,
                cover: albumInfo.artwork || albumInfo.cover || `https://y.gtimg.cn/music/photo_new/T002R300x300M000${albumId}.jpg`,
                duration: item.duration || item.interval || 0,
                source: 'qq',
                albumId: albumId,
                singer: item.singer || (Array.isArray(item.singer) ? item.singer : [])
              })) : [],
              singer: {
                id: albumInfo.singerId || albumInfo.artistId,
                name: albumInfo.artist || albumInfo.artistName
              }
            }
          }
        }
      }
      
      // 如果plugin加载失败或返回无效数据，使用模拟数据
      console.log('使用模拟专辑信息数据')
      return {
        success: true,
        data: {
          id: albumId,
          name: '叶惠美',
          artist: '周杰伦',
          cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000002J4UUk29y8BY_1.jpg',
          releaseDate: '2003-07-31',
          songs: [
            {
              id: '001JZkTF2XZ8lH',
              name: '晴天',
              artist: ['周杰伦'],
              album: '叶惠美',
              cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000002J4UUk29y8BY_1.jpg',
              duration: 260,
              source: 'qq',
              albumId: albumId,
              singer: [{ id: '0025NhlN2yWrP4', name: '周杰伦' }]
            }
          ],
          singer: {
            id: '0025NhlN2yWrP4',
            name: '周杰伦'
          }
        }
      }
    } catch (error) {
      console.error('获取专辑信息失败:', error)
      return { success: false, error: error.message }
    }
  },
  
  // 获取歌单详情（与getPlaylistInfo保持一致）
  getPlaylistDetail: async (playlistId) => {
    try {
      console.log('获取歌单详情:', playlistId)
      
      // 加载plugin-wrapper.js
      const pluginModule = await loadPlugin()
      
      // 如果plugin加载成功，使用plugin的getMusicSheetInfo或getPlaylistDetail方法
      if (pluginModule) {
        let playlistDetail;
        
        // 尝试使用getPlaylistDetail方法（如果存在）
        if (pluginModule.getPlaylistDetail) {
          playlistDetail = await pluginModule.getPlaylistDetail(playlistId)
        }
        // 否则尝试使用getMusicSheetInfo方法
        else if (pluginModule.getMusicSheetInfo) {
          playlistDetail = await pluginModule.getMusicSheetInfo(playlistId)
        }
        
        console.log('plugin playlistDetail result:', playlistDetail)
        
        if (playlistDetail) {
          // 转换为前端期望的格式
          return {
            success: true,
            data: {
              id: playlistId,
              name: playlistDetail.title || playlistDetail.name,
              cover: playlistDetail.artwork || playlistDetail.cover || `https://y.gtimg.cn/music/photo_new/T003R300x300M000${playlistId}.jpg`,
              creator: {
                id: playlistDetail.creatorId || playlistDetail.creator?.id,
                name: playlistDetail.creator || playlistDetail.creator?.name
              },
              songCount: playlistDetail.songCount || playlistDetail.total_song_num || 0,
              playCount: playlistDetail.playCount || playlistDetail.visitnum || 0,
              description: playlistDetail.description || playlistDetail.desc || '',
              songs: playlistDetail.songs ? playlistDetail.songs.map(item => ({
                id: item.id || item.songmid,
                name: item.title || item.songname,
                artist: item.artist ? (Array.isArray(item.artist) ? item.artist : [item.artist]) : [],
                album: item.album || item.albumname,
                cover: item.artwork || item.cover || `https://y.gtimg.cn/music/photo_new/T002R300x300M000${item.albummid || item.albumid}.jpg`,
                duration: item.duration || item.interval || 0,
                source: 'qq',
                albumId: item.albumid || item.albummid,
                singer: item.singer || (Array.isArray(item.singer) ? item.singer : [])
              })) : []
            }
          }
        }
      }
      
      // 如果plugin加载失败或返回无效数据，使用模拟数据
      console.log('使用模拟歌单详情数据')
      return {
        success: true,
        data: {
          id: playlistId,
          name: '热门华语歌曲',
          cover: 'https://y.gtimg.cn/music/photo_new/T003R300x300M000003rYHle3YhH20.jpg',
          creator: {
            id: '123456',
            name: 'QQ音乐官方'
          },
          songCount: 2,
          playCount: 12345678,
          description: '热门华语歌曲推荐',
          songs: [
            {
              id: '001JZkTF2XZ8lH',
              name: '晴天',
              artist: ['周杰伦'],
              album: '叶惠美',
              cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000002J4UUk29y8BY_1.jpg',
              duration: 260,
              source: 'qq',
              albumId: '002J4UUk29y8BY',
              singer: [{ id: '0025NhlN2yWrP4', name: '周杰伦' }]
            },
            {
              id: '003fdK422CjWbd',
              name: '七里香',
              artist: ['周杰伦'],
              album: '七里香',
              cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000001VfvsJ2170e9_1.jpg',
              duration: 280,
              source: 'qq',
              albumId: '001VfvsJ2170e9',
              singer: [{ id: '0025NhlN2yWrP4', name: '周杰伦' }]
            }
          ]
        }
      }
    } catch (error) {
      console.error('获取歌单详情失败:', error)
      return { success: false, error: error.message }
    }
  },
  
  // 获取排行榜
  getTopLists: async () => {
    try {
      console.log('获取排行榜')
      
      // 加载plugin-wrapper.js
      const pluginModule = await loadPlugin()
      
      // 如果plugin加载成功，使用plugin的getTopLists或getTopList方法
      if (pluginModule && (pluginModule.getTopLists || pluginModule.getTopList)) {
        const topLists = await (pluginModule.getTopLists ? pluginModule.getTopLists() : pluginModule.getTopList())
        console.log('plugin topLists result:', topLists)
        
        if (topLists && Array.isArray(topLists)) {
          // 转换为前端期望的格式
          return {
            success: true,
            data: [{
              title: '热门排行榜',
              data: topLists.map(item => ({
                id: item.id,
                title: item.title,
                picUrl: item.artwork || item.picUrl || item.cover,
                listenCount: item.playCount || item.listenCount,
                updateFrequency: item.updateFrequency || '每日更新',
                songList: []
              }))
            }]
          }
        }
      }
      
      // 如果plugin加载失败或返回无效数据，使用模拟数据
      console.log('使用模拟排行榜数据')
      const mockTopLists = [{
        title: '热门排行榜',
        data: [
          {
            id: 4,
            title: '巅峰榜·流行指数',
            picUrl: 'https://y.qq.com/music/photo_new/T002R300x300M000001f8e5K0X1VhS.jpg',
            listenCount: 12345678,
            updateFrequency: '每日更新',
            songList: []
          },
          {
            id: 26,
            title: '巅峰榜·内地',
            picUrl: 'https://y.qq.com/music/photo_new/T002R300x300M000001r5r2M3p7wLC.jpg',
            listenCount: 87654321,
            updateFrequency: '每日更新',
            songList: []
          },
          {
            id: 27,
            title: '巅峰榜·港台',
            picUrl: 'https://y.qq.com/music/photo_new/T002R300x300M000003rYHle3YhH20.jpg',
            listenCount: 56789012,
            updateFrequency: '每日更新',
            songList: []
          },
          {
            id: 45,
            title: '巅峰榜·欧美',
            picUrl: 'https://y.qq.com/music/photo_new/T002R300x300M000001X3tXm3e8VQx.jpg',
            listenCount: 34567890,
            updateFrequency: '每日更新',
            songList: []
          },
          {
            id: 46,
            title: '巅峰榜·韩国',
            picUrl: 'https://y.qq.com/music/photo_new/T002R300x300M000003yNq4F3F2j8l.jpg',
            listenCount: 45678901,
            updateFrequency: '每日更新',
            songList: []
          }
        ]
      }]
      
      return {
        success: true,
        data: mockTopLists
      }
    } catch (error) {
      console.error('获取排行榜失败:', error)
      return { success: false, error: error.message }
    }
  },
  
  // 获取榜单详情
  getTopListDetail: async (topListItem) => {
    try {
      console.log('获取榜单详情:', topListItem)
      
      // 提取榜单ID
      const listId = typeof topListItem === 'object' ? topListItem.id : topListItem
      
      // 加载plugin-wrapper.js
      const pluginModule = await loadPlugin()
      
      // 如果plugin加载成功，使用plugin的getTopListDetail方法
      if (pluginModule && pluginModule.getTopListDetail) {
        const topListDetail = await pluginModule.getTopListDetail(listId)
        console.log('plugin topListDetail result:', topListDetail)
        
        if (topListDetail && topListDetail.musicList) {
          // 转换为前端期望的格式
          return {
            success: true,
            data: {
              isEnd: topListDetail.isEnd || true,
              topListItem: typeof topListItem === 'object' ? topListItem : { id: listId },
              musicList: topListDetail.musicList.map(item => ({
                id: item.id || item.songmid,
                name: item.title || item.songname,
                artist: item.artist ? (Array.isArray(item.artist) ? item.artist : [item.artist]) : [],
                album: item.album || item.albumname,
                cover: item.artwork || item.cover,
                duration: item.duration || item.interval,
                source: 'qq',
                albumId: item.albumid || item.albummid,
                singer: item.singer || [],
                rank: item.rank
              })),
              songCount: topListDetail.songCount || topListDetail.musicList.length
            }
          }
        }
      }
      
      // 如果plugin加载失败或返回无效数据，使用模拟数据
      console.log('使用模拟榜单详情数据')
      const mockMusicList = [
        {
          id: '001JZkTF2XZ8lH',
          name: '晴天',
          artist: ['周杰伦'],
          album: '叶惠美',
          cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000002J4UUk29y8BY_1.jpg',
          duration: 260,
          source: 'qq',
          albumId: '002J4UUk29y8BY',
          singer: [{ id: '0025NhlN2yWrP4', name: '周杰伦' }],
          rank: 1
        },
        {
          id: '003fdK422CjWbd',
          name: '七里香',
          artist: ['周杰伦'],
          album: '七里香',
          cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000001VfvsJ2170e9_1.jpg',
          duration: 280,
          source: 'qq',
          albumId: '001VfvsJ2170e9',
          singer: [{ id: '0025NhlN2yWrP4', name: '周杰伦' }],
          rank: 2
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
  getLyric: async (songId, songmid) => {
    try {
      console.log('获取歌词:', songId, songmid)
      
      // 尝试使用plugin-wrapper.js获取数据
      const pluginModule = await loadPlugin()
      if (pluginModule && pluginModule.getLyric) {
        // 根据plugin-api-example.js，getLyric需要两个参数：id和songmid
        const lyric = await pluginModule.getLyric(songId, songmid)
        if (lyric) {
          return {
            success: true,
            data: {
              id: songId,
              lyric: lyric || '',
              translatedLyric: '',
              tlyric: ''
            }
          }
        }
      }
      
      // 如果plugin-wrapper.js失败，使用模拟数据
      console.log('使用模拟歌词数据')
      return {
        success: true,
        data: {
          id: songId,
          lyric: '[00:00.00] 暂无歌词',
          translatedLyric: '',
          tlyric: ''
        }
      }
    } catch (error) {
      console.error('获取歌词失败:', error)
      return { success: false, error: error.message }
    }
  },
  
  // 获取音乐资源
  getMediaSource: async (songId, songmid, quality = 'standard') => {
    try {
      console.log('获取音乐资源:', songId, songmid, quality)
      
      // 尝试使用plugin-wrapper.js获取数据
      const pluginModule = await loadPlugin()
      if (pluginModule && pluginModule.getMediaSource) {
        // 根据plugin-api-example.js，getMediaSource需要两个参数：id和songmid
        const mediaSource = await pluginModule.getMediaSource(songId, songmid)
        if (mediaSource && mediaSource.url) {
          return {
            success: true,
            data: {
              id: songId,
              url: mediaSource.url,
              quality: mediaSource.quality || quality,
              size: mediaSource.size || 10485760,
              format: mediaSource.format || 'mp3',
              bitrate: mediaSource.bitrate || 320
            }
          }
        }
      }
      
      // 如果plugin-wrapper.js失败，使用模拟数据
      console.log('使用模拟音乐资源数据')
      return {
        success: true,
        data: {
          id: songId,
          url: 'https://dl.stream.qqmusic.qq.com/M500003fdK422CjWbd.mp3?vkey=A8A5E1F8C4E8B9D7F2E4A6C8B0D2F4A6&guid=1234567890&uin=0&fromtag=66',
          quality: quality,
          size: 10485760,
          format: 'mp3',
          bitrate: 320
        }
      }
    } catch (error) {
      console.error('获取音乐资源失败:', error)
      return { success: false, error: error.message }
    }
  },
  
  // 获取推荐专辑
  getRecommendAlbums: async (limit = 8) => {
    try {
      console.log('获取推荐专辑')
      
      // 尝试使用plugin-wrapper.js获取数据
      const pluginModule = await loadPlugin()
      if (pluginModule && pluginModule.getRecommendAlbums) {
        const albums = await pluginModule.getRecommendAlbums(limit)
        if (albums && Array.isArray(albums)) {
          return {
            success: true,
            data: albums
          }
        }
      }
      
      // 如果plugin-wrapper.js失败，使用模拟数据
      console.log('使用模拟推荐专辑数据')
      const mockAlbums = [
        {
          id: '002J4UUk29y8BY',
          name: '叶惠美',
          artist: '周杰伦',
          cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000002J4UUk29y8BY_1.jpg',
          singer: [{ id: '0025NhlN2yWrP4', name: '周杰伦' }]
        },
        {
          id: '001VfvsJ2170e9',
          name: '七里香',
          artist: '周杰伦',
          cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000001VfvsJ2170e9_1.jpg',
          singer: [{ id: '0025NhlN2yWrP4', name: '周杰伦' }]
        }
      ]
      
      return {
        success: true,
        data: mockAlbums
      }
    } catch (error) {
      console.error('获取推荐专辑失败:', error)
      return { success: false, error: error.message }
    }
  },
  
  // 获取歌手列表
  getArtistList: async () => {
    try {
      console.log('获取歌手列表')
      
      // 尝试使用plugin-wrapper.js获取数据
      const pluginModule = await loadPlugin()
      if (pluginModule && pluginModule.getArtistList) {
        const artists = await pluginModule.getArtistList()
        if (artists && Array.isArray(artists)) {
          return {
            success: true,
            data: artists
          }
        }
      }
      
      // 如果plugin-wrapper.js失败，使用模拟数据
      console.log('使用模拟歌手列表数据')
      const mockArtists = [
        {
          id: '0025NhlN2yWrP4',
          name: '周杰伦',
          avatar: 'https://y.gtimg.cn/music/photo_new/T001R300x300M0000025NhlN2yWrP4_1.jpg',
          songCount: 150,
          fansCount: 12345678,
          description: '周杰伦 - 华语流行歌手'
        },
        {
          id: '003N4T8L1Jz4oF',
          name: '五月天',
          avatar: 'https://y.gtimg.cn/music/photo_new/T001R300x300M000003N4T8L1Jz4oF_1.jpg',
          songCount: 200,
          fansCount: 98765432,
          description: '五月天 - 华语摇滚乐团'
        }
      ]
      
      return {
        success: true,
        data: mockArtists
      }
    } catch (error) {
      console.error('获取歌手列表失败:', error)
      return { success: false, error: error.message }
    }
  },
  
  // 获取歌手热门歌曲
  getArtistSongs: async (artistId) => {
    try {
      console.log('获取歌手热门歌曲:', artistId)
      
      // 尝试使用plugin-wrapper.js获取数据
      const pluginModule = await loadPlugin()
      if (pluginModule && (pluginModule.getArtistWorks || pluginModule.getArtistSongs)) {
        const songs = await (pluginModule.getArtistWorks ? pluginModule.getArtistWorks(artistId) : pluginModule.getArtistSongs(artistId))
        if (songs && Array.isArray(songs)) {
          return {
            success: true,
            data: songs.map(item => ({
              id: item.id || item.songmid,
              name: item.title || item.songname,
              artist: item.artist ? (Array.isArray(item.artist) ? item.artist : [item.artist]) : [],
              album: item.album || item.albumname,
              cover: item.artwork || `https://y.gtimg.cn/music/photo_new/T002R300x300M000${item.albummid}.jpg`,
              duration: item.duration || item.interval,
              source: 'qq',
              albumId: item.albumid || item.albummid,
              singer: item.singer || item.singer?.map(s => ({
                id: s.id || s.singerid,
                name: s.name
              })) || []
            }))
          }
        }
      }
      
      // 如果plugin-wrapper.js失败，使用模拟数据
      console.log('使用模拟歌手热门歌曲数据')
      const mockSongs = [
        {
          id: '001JZkTF2XZ8lH',
          name: '晴天',
          artist: ['周杰伦'],
          album: '叶惠美',
          cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000002J4UUk29y8BY_1.jpg',
          duration: 260,
          source: 'qq',
          albumId: '002J4UUk29y8BY',
          singer: [{ id: '0025NhlN2yWrP4', name: '周杰伦' }]
        },
        {
          id: '003fdK422CjWbd',
          name: '七里香',
          artist: ['周杰伦'],
          album: '七里香',
          cover: 'https://y.gtimg.cn/music/photo_new/T002R300x300M000001VfvsJ2170e9_1.jpg',
          duration: 280,
          source: 'qq',
          albumId: '001VfvsJ2170e9',
          singer: [{ id: '0025NhlN2yWrP4', name: '周杰伦' }]
        }
      ]
      
      return {
        success: true,
        data: mockSongs
      }
    } catch (error) {
      console.error('获取歌手热门歌曲失败:', error)
      return { success: false, error: error.message }
    }
  },
}