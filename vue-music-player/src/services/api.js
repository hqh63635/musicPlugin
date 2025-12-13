// 音乐API服务
// 导入插件包装器
import plugin from '../utils/plugin-wrapper.js';

/**
 * 搜索音乐相关内容
 * @param {string} keyword - 搜索关键词
 * @param {number} page - 页码
 * @param {string} type - 搜索类型：music(歌曲), album(专辑), artist(艺术家), sheet(歌单), lyric(歌词)
 * @returns {Promise<Object>} 搜索结果
 */
export async function search(keyword, page = 1, type = 'music') {
  try {
    if (!keyword || typeof keyword !== 'string') {
      console.error('搜索失败: 关键词不能为空且必须是字符串');
      return { total: 0, data: [] };
    }
    return await plugin.search(keyword, page, type);
  } catch (error) {
    console.error('搜索失败:', error);
    return { total: 0, data: [] };
  }
}

/**
 * 获取媒体源（播放链接）
 * @param {Object} song - 歌曲对象，包含id和songmid
 * @param {string} quality - 音质
 * @returns {Promise<Object>} 媒体源信息
 */
export async function getMediaSource(song, quality = 'standard') {
  try {
    if (!song || typeof song !== 'object') {
      console.error('获取媒体源失败: 歌曲对象必须是有效的对象');
      return null;
    }

    // 验证音质参数，确保是有效的字符串
    if (typeof quality !== 'string' || quality.trim() === '') {
      quality = 'standard';
      console.warn('音质参数无效，使用默认值: standard');
    } else {
      quality = quality.trim();
    }

    // Handle property name differences (songId vs id, songmid vs songMid/singerMid)
    const songId = song.songId || song.id;
    const songmid = song.songmid || song.songMid || song.singerMid;

    // 更严格的参数验证：确保songId和songmid是有效的非空值
    if (
      songId === undefined ||
      songId === null ||
      (typeof songId === 'string' && songId.trim() === '') ||
      songmid === undefined ||
      songmid === null ||
      (typeof songmid === 'string' && songmid.trim() === '')
    ) {
      console.error('获取媒体源失败: 歌曲对象缺少必要的ID属性');
      console.error('可用属性:', Object.keys(song));
      console.error('songId:', songId, 'songmid:', songmid);
      return null;
    }

    // 将songId和songmid转换为字符串，确保格式一致
    const normalizedSong = {
      ...song,
      id: String(songId).trim(),
      songmid: String(songmid).trim(),
    };
    const normalizedQuality = quality.toLowerCase();

    console.log('获取媒体源参数:', { song: normalizedSong, quality: normalizedQuality });

    // 验证plugin实例和getMediaSource方法的有效性
    if (!plugin || typeof plugin !== 'object') {
      console.error('获取媒体源失败: plugin实例无效');
      // 尝试返回原始URL作为后备
      if (song.url || song.qualities?.[normalizedQuality]?.url) {
        return {
          url: song.url || song.qualities?.[normalizedQuality]?.url,
          headers: {},
          userAgent: '',
        };
      }
      return null;
    }

    if (typeof plugin.getMediaSource !== 'function') {
      console.error('获取媒体源失败: plugin.getMediaSource不是有效的函数');
      console.error(
        'plugin的可用方法:',
        Object.keys(plugin).filter(key => typeof plugin[key] === 'function')
      );
      // 尝试返回原始URL作为后备
      if (song.url || song.qualities?.[normalizedQuality]?.url) {
        return {
          url: song.url || song.qualities?.[normalizedQuality]?.url,
          headers: {},
          userAgent: '',
        };
      }
      return null;
    }

    try {
      // 根据TypeScript参考，应该传递整个歌曲对象和音质字符串
      const result = await plugin.getMediaSource(normalizedSong, normalizedQuality);
      console.log('plugin.getMediaSource返回结果:', result);

      if (!result || !result.url) {
        throw new Error('NOT RETRY');
      }

      // 格式化返回结果以匹配预期格式
      return {
        url: result.url,
        headers: result.headers || {},
        userAgent: result.headers?.['user-agent'] || '',
      };
    } catch (pluginError) {
      console.error('调用plugin.getMediaSource时发生错误:', pluginError);
      return null;
    }
  } catch (error) {
    console.error('获取媒体源失败:', error);
    return null;
  }
}

/**
 * 获取歌词
 * @param {Object} song - 歌曲对象，包含id和songmid
 * @returns {Promise<Object>} 歌词信息
 */
export async function getLyric(song) {
  try {
    if (!song || typeof song !== 'object') {
      console.error('获取歌词失败: 歌曲对象必须是有效的对象');
      return null;
    }

    // Handle property name differences and normalize properties
    const normalizedSong = {
      ...song,
      // 确保id和songmid属性存在且有效
      id: song.songId || song.id,
      songmid: song.songmid || song.songMid || song.singerMid,
    };

    // 更严格的参数验证：确保id和songmid是有效的非空值
    if (
      normalizedSong.id === undefined ||
      normalizedSong.id === null ||
      (typeof normalizedSong.id === 'string' && normalizedSong.id.trim() === '') ||
      normalizedSong.songmid === undefined ||
      normalizedSong.songmid === null ||
      (typeof normalizedSong.songmid === 'string' && normalizedSong.songmid.trim() === '')
    ) {
      console.error('获取歌词失败: 歌曲对象缺少必要的ID属性');
      console.error('可用属性:', Object.keys(song));
      console.error('id:', normalizedSong.id, 'songmid:', normalizedSong.songmid);
      return null;
    }

    // 将id和songmid转换为字符串，确保格式一致
    normalizedSong.id = String(normalizedSong.id).trim();
    normalizedSong.songmid = String(normalizedSong.songmid).trim();

    // 新增songmid有效性校验
    if (!normalizedSong.songmid) {
      console.error('获取歌词失败: songmid不能为空');
      return { lyric: '', translation: '' };
    }

    console.log('获取歌词参数:', { song: normalizedSong });

    // 验证plugin实例和getLyric方法的有效性
    if (!plugin || typeof plugin !== 'object') {
      console.error('获取歌词失败: plugin实例无效');
      return null;
    }

    if (typeof plugin.getLyric !== 'function') {
      console.error('获取歌词失败: plugin.getLyric不是有效的函数');
      console.error(
        'plugin的可用方法:',
        Object.keys(plugin).filter(key => typeof plugin[key] === 'function')
      );
      return null;
    }

    // 根据getMediaSource的模式，传递整个歌曲对象
    try {
      const result = await plugin.getLyric(normalizedSong);
      console.log('plugin.getLyric返回结果:', result);

      // 验证返回结果的格式是否正确
      if (typeof result === 'string') {
        // 处理字符串格式的歌词（兼容example.js的返回格式）
        return { lyric: result, translation: '' };
      } else if (result && typeof result === 'object') {
        // 映射rawLrc到lyric并验证
        const lyric = typeof result.rawLrc === 'string' ? result.rawLrc : '';
        return { lyric, translation: result.translation || '' };
      }

      console.warn('plugin.getLyric返回的结果不是有效的对象或字符串:', result);
      return { lyric: '', translation: '' };
      return null;
    } catch (pluginError) {
      console.error('调用plugin.getLyric时发生错误:', pluginError);

      // 详细记录错误信息，帮助调试
      console.error('错误堆栈:', pluginError.stack);

      // 返回空歌词对象避免下游错误
      return { lyric: '', translation: '' };
      return null;
    }
  } catch (error) {
    console.error('获取歌词失败:', error);
    return null;
  }
}

/**
 * 获取专辑信息
 * @param {Object} album - 专辑对象，包含albumid和albummid
 * @returns {Promise<Object>} 专辑信息
 */
export async function getAlbumInfo(album) {
  try {
    if (!album || typeof album !== 'object') {
      console.error('获取专辑信息失败: 专辑对象必须是有效的对象');
      return null;
    }

    // Handle property name differences and normalize properties
    const normalizedAlbum = {
      ...album,
      // 确保albumid和albummid属性存在且有效
      albumid: album.albumId || album.albumid,
      albummid: album.albumMid || album.albummid,
    };

    // 更严格的参数验证：确保albumid和albummid是有效的非空值
    if (
      normalizedAlbum.albumid === undefined ||
      normalizedAlbum.albumid === null ||
      (typeof normalizedAlbum.albumid === 'string' && normalizedAlbum.albumid.trim() === '') ||
      normalizedAlbum.albummid === undefined ||
      normalizedAlbum.albummid === null ||
      (typeof normalizedAlbum.albummid === 'string' && normalizedAlbum.albummid.trim() === '')
    ) {
      console.error('获取专辑信息失败: 专辑对象缺少必要的ID属性');
      console.error('可用属性:', Object.keys(album));
      console.error('albumid:', normalizedAlbum.albumid, 'albummid:', normalizedAlbum.albummid);
      return null;
    }

    // 将albumid和albummid转换为字符串，确保格式一致
    normalizedAlbum.albumid = String(normalizedAlbum.albumid).trim();
    normalizedAlbum.albummid = String(normalizedAlbum.albummid).trim();

    console.log('获取专辑信息参数:', { album: normalizedAlbum });

    // 验证plugin实例和getAlbumInfo方法的有效性
    if (!plugin || typeof plugin !== 'object') {
      console.error('获取专辑信息失败: plugin实例无效');
      return null;
    }

    if (typeof plugin.getAlbumInfo !== 'function') {
      console.error('获取专辑信息失败: plugin.getAlbumInfo不是有效的函数');
      console.error(
        'plugin的可用方法:',
        Object.keys(plugin).filter(key => typeof plugin[key] === 'function')
      );
      return null;
    }

    // 根据getMediaSource的模式，传递整个专辑对象
    try {
      const result = await plugin.getAlbumInfo(normalizedAlbum);
      console.log('plugin.getAlbumInfo返回结果:', result);
      return result;
    } catch (pluginError) {
      console.error('调用plugin.getAlbumInfo时发生错误:', pluginError);
      return null;
    }
  } catch (error) {
    console.error('获取专辑信息失败:', error);
    return null;
  }
}

/**
 * 获取排行榜列表
 * @returns {Promise<Array>} 排行榜列表
 */
export async function getTopLists() {
  try {
    return await plugin.getTopLists();
  } catch (error) {
    console.error('获取排行榜失败:', error);
    return [];
  }
}

/**
 * 获取排行榜详情
 * @param {Object} topList - 排行榜对象，包含id
 * @returns {Promise<Object>} 排行榜详情
 */
export async function getTopListDetail(topList) {
  try {
    if (!topList || !topList.id) {
      console.error('获取排行榜详情失败: 排行榜对象无效或缺少id属性');
      return null;
    }
    return await plugin.getTopListDetail(topList);
  } catch (error) {
    console.error('获取排行榜详情失败:', error);
    return null;
  }
}

/**
 * 获取艺术家作品
 * @param {Object} artist - 艺术家对象
 * @param {number} page - 页码
 * @param {string} type - 作品类型：music(歌曲), album(专辑)
 * @returns {Promise<Object>} 艺术家作品
 */
export async function getArtistWorks(artist, page = 1, type = 'music') {
  try {
    if (!artist) {
      console.error('获取艺术家作品失败: 艺术家对象不能为空');
      return { isEnd: true, data: [] };
    }
    return await plugin.getArtistWorks(artist, page, type);
  } catch (error) {
    console.error('获取艺术家作品失败:', error);
    return { isEnd: true, data: [] };
  }
}

/**
 * 导入歌单
 * @param {string} url - 歌单链接或ID
 * @returns {Promise<Array>} 歌单歌曲列表
 */
export async function importMusicSheet(url) {
  try {
    if (!url || typeof url !== 'string') {
      console.error('导入歌单失败: 歌单链接或ID不能为空且必须是字符串');
      return [];
    }
    return await plugin.importMusicSheet(url);
  } catch (error) {
    console.error('导入歌单失败:', error);
    return [];
  }
}

/**
 * 获取歌单详情
 * @param {string} platform - 平台名称
 * @param {string} id - 歌单ID
 * @param {number} page - 页码
 * @returns {Promise<Object>} 歌单详情和歌曲列表
 */
export async function getMusicSheetInfo(platform, id, page = 1) {
  try {
    if (!platform || !id) {
      console.error('获取歌单详情失败: 平台和ID不能为空');
      return { sheetItem: null, musicList: [], isEnd: true };
    }
    return await plugin.getMusicSheetInfo({ platform, id }, page);
  } catch (error) {
    console.error('获取歌单详情失败:', error);
    return { sheetItem: null, musicList: [], isEnd: true };
  }
}

/**
 * 获取推荐歌单标签
 * @returns {Promise<Object>} 推荐歌单标签
 */
export async function getRecommendSheetTags() {
  try {
    return await plugin.getRecommendSheetTags();
  } catch (error) {
    console.error('获取推荐歌单标签失败:', error);
    return { pinned: [], data: [] };
  }
}

/**
 * 根据标签获取推荐歌单
 * @param {Object} tag - 标签对象
 * @param {number} page - 页码
 * @returns {Promise<Object>} 推荐歌单
 */
export async function getRecommendSheetsByTag(tag, page = 1) {
  try {
    if (!tag) {
      console.error('获取推荐歌单失败: 标签对象不能为空');
      return { isEnd: true, data: [] };
    }
    return await plugin.getRecommendSheetsByTag(tag, page);
  } catch (error) {
    console.error('获取推荐歌单失败:', error);
    return { isEnd: true, data: [] };
  }
}

/**
 * 获取QQ音乐歌手详细信息
 * @param {string} singerMid - 歌手ID
 * @returns {Promise<Object>} 歌手详细信息
 */
export async function getQQArtistInfo(singerMid) {
  try {
    const timestamp = new Date().getTime();
    const url = `/qqmusic/cgi-bin/musics.fcg?_=${timestamp}&encoding=ag-1&sign=zzc04a5944br39mj1iilqnmcylsfnrwsd5jiu94a149ee`;
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`HTTP错误: ${response.status}`);
    }
    const data = await response.json();
    // 解析歌手信息响应数据
    return {
      id: data.singerId || singerMid,
      name: data.singerName,
      avatar: data.singerAvatar,
      desc: data.singerDesc,
      musicSize: data.songCount,
      albumSize: data.albumCount,
    };
  } catch (error) {
    console.error('获取QQ音乐歌手信息失败:', error);
    return null;
  }
}

/**
 * 获取歌手分类列表
 */
export const getSingerCategories = async () => {
  try {
    const timestamp = new Date().getTime();
    const url = `/qqapi/cgi-bin/musicu.fcg?_=${timestamp}&encoding=ag-1&sign=zzc04a5944br39mj1iilqnmcylsfnrwsd5jiu94a149ee`;
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({
        data: JSON.stringify({
          comm: { ct: 24, cv: 0 },
          singerList: {
            module: 'Music.SingerListServer',
            method: 'get_singer_list',
            param: { area: 0, sex: 0, genre: 0, index: 1, sin: 0, cur_page: 1 },
          },
        }),
      }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    return data.singerList?.data?.tags || [];
  } catch (error) {
    console.error('获取歌手分类失败:', error);
    return [];
  }
};

/**
 * 根据分类获取歌手列表
 * @param {Object} params - 查询参数
 * @param {number} params.area - 地区分类(-100:全部)
 * @param {number} params.sex - 性别分类(-100:全部)
 * @param {number} params.genre - 流派分类(-100:全部)
 * @param {number} params.index - 索引分类(-100:全部)
 * @param {number} params.pageNo - 页码
 */
export const getSingerList = async (params = {}) => {
  try {
    const { area = -100, sex = -100, genre = -100, index = -100, pageNo = 1 } = params;
    const timestamp = new Date().getTime();
    const url = `/qqmusic/cgi-bin/musicu.fcg?_=${timestamp}`;
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({
        data: JSON.stringify({
          comm: { ct: 24, cv: 0 },
          singerList: {
            module: 'Music.SingerListServer',
            method: 'get_singer_list',
            param: {
              area: Number(area),
              sex: Number(sex),
              genre: Number(genre),
              index: Number(index),
              sin: (pageNo - 1) * 80,
              cur_page: Number(pageNo),
            },
          },
        }),
      }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    const singerData = data.singerList?.data || {};
    return {
      list: singerData.singerlist || [],
      total: singerData.total || 0,
      curPage: singerData.cur_page || 1,
    };
  } catch (error) {
    console.error('获取歌手列表失败:', error);
    return { list: [], total: 0, curPage: 1 };
  }
};

// 导出所有API方法
export default {
  search,
  getMediaSource,
  getLyric,
  getAlbumInfo,
  getTopLists,
  getTopListDetail,
  getArtistWorks,
  importMusicSheet,
  getRecommendSheetTags,
  getRecommendSheetsByTag,
  getQQArtistInfo,
  getSingerCategories,
  getSingerList,
  getMusicSheetInfo,
};
