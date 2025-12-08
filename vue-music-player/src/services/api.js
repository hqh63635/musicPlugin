import qqDecoded from '@/utils/qq-decoded.js'

// 创建安全的函数执行环境
function createFunctionFromCode(code) {
  // 提取函数体内容
  const functionBody = code.replace(/^async\s+(function\s+\w+|\w+)\s*\([^)]*\)\s*\{/, '').replace(/}$/, '').trim();

  // 创建函数，模拟plugin-wrapper.js中的环境
  return async function(...args) {
    const require = (module) => {
      if (module === 'axios') return request;
      throw new Error(`Module ${module} not supported`);
    };

    const console = { log: (...args) => {}, error: (...args) => {} };
    const env = { getUserVariables: () => ({}) };
    const process = { platform: 'win32', env };
    const module = { exports: {} };
    const exports = module.exports;

    // 定义qq-decoded.js中使用的辅助函数
    const _0x2d9c31 = (num, str) => str; // 模拟混淆函数
    const CryptoJs = { enc: { Utf8: {} }, AES: { decrypt: () => ({ toString: () => '' }) } };
    const he = { decode: (str) => str };

    try {
      // 使用Function构造器创建函数
      const func = new Function('require', '__musicfree_require', 'module', 'exports', 'console', 'env', 'process', '_0x2d9c31', 'CryptoJs', 'he',
        `return async function() { ${functionBody} };`
      )(require, require, module, exports, console, env, process, _0x2d9c31, CryptoJs, he);

      return await func(...args);
    } catch (error) {
      console.error('函数执行失败:', error);
      throw error;
    }
  };
}

// 解析qq-decoded.js中的方法
const qqMethods = {
  search: createFunctionFromCode(qqDecoded.search),
  getMediaSource: createFunctionFromCode(qqDecoded.getMediaSource),
  getLyric: createFunctionFromCode(qqDecoded.getLyric),
  getAlbumInfo: createFunctionFromCode(qqDecoded.getAlbumInfo),
  getArtistWorks: createFunctionFromCode(qqDecoded.getArtistWorks),
  importMusicSheet: createFunctionFromCode(qqDecoded.importMusicSheet),
  getTopLists: createFunctionFromCode(qqDecoded.getTopLists),
  getTopListDetail: createFunctionFromCode(qqDecoded.getTopListDetail),
  getRecommendSheetTags: createFunctionFromCode(qqDecoded.getRecommendSheetTags),
  getRecommendSheetsByTag: createFunctionFromCode(qqDecoded.getRecommendSheetsByTag),
  getMusicSheetInfo: createFunctionFromCode(qqDecoded.getMusicSheetInfo)
};


// 获取排行榜列表
export async function getTopLists() {
  try {
    const result = await qqMethods.getTopLists();
    return {success: true, data: result };
  } catch (error) {
    console.error('获取排行榜列表失败:', error);
    return { success: false, error: error.message };
  }
}

// 获取排行榜详情
export async function getTopListDetail(toplistId) {
  try {
    const result = await qqMethods.getTopListDetail({ id: toplistId });
    return { success: true, data: result };
  } catch (error) {
    console.error('获取排行榜详情失败:', error);
    return { success: false, error: error.message };
  }
}

// 获取推荐歌单标签
export async function getRecommendSheetTags() {
  try {
    const result = await qqMethods.getRecommendSheetTags();
    return { success: true, data: result };
  } catch (error) {
    console.error('获取推荐歌单标签失败:', error);
    return { success: false, error: error.message };
  }
}

// 根据标签获取推荐歌单
export async function getRecommendSheetsByTag(tagId, page = 1, pageSize = 20) {
  try {
    const result = await qqMethods.getRecommendSheetsByTag(tagId, page, pageSize);
    return { success: true, data: result };
  } catch (error) {
    console.error('根据标签获取推荐歌单失败:', error);
    return { success: false, error: error.message };
  }
}

// 获取歌单信息
export async function getMusicSheetInfo(sheetId) {
  try {
    const result = await qqMethods.getMusicSheetInfo({ id: sheetId });
    return { success: true, data: result };
  } catch (error) {
    console.error('获取歌单信息失败:', error);
    return { success: false, error: error.message };
  }
}

// 实现搜索方法
export async function search(keyword, page = 1, pageSize = 20) {
  try {
    const result = await qqMethods.search(keyword, page, pageSize, 'music');
    return { success: true, data: result };
  } catch (error) {
    console.error('搜索音乐失败:', error);
    return { success: false, error: error.message };
  }
}

// 获取媒体资源
export async function getMediaSource(songId, quality = 'standard') {
  try {
    const result = await qqMethods.getMediaSource({ songmid: songId }, quality);
    return { success: true, data: result };
  } catch (error) {
    console.error('获取媒体资源失败:', error);
    return { success: false, error: error.message };
  }
}

// 获取歌词
export async function getLyric(songId) {
  try {
    const result = await qqMethods.getLyric({ songmid: songId });
    return { success: true, data: result };
  } catch (error) {
    console.error('获取歌词失败:', error);
    return { success: false, error: error.message };
  }
}

// 获取专辑信息
export async function getAlbumInfo(albumId) {
  try {
    const result = await qqMethods.getAlbumInfo({ albummid: albumId });
    return { success: true, data: result };
  } catch (error) {
    console.error('获取专辑信息失败:', error);
    return { success: false, error: error.message };
  }
}
// 获取歌手作品
export async function getArtistWorks(artistId, page = 1, pageSize = 20) {
  try {
    const result = await qqMethods.getArtistWorks(artistId, page, pageSize);
    return { success: true, data: result };
  } catch (error) {
    console.error('获取歌手作品失败:', error);
    return { success: false, error: error.message };
  }
}

// 导入歌单
export async function importMusicSheet(sheetUrl) {
  try {
    const result = await qqMethods.importMusicSheet(sheetUrl);
    return { success: true, data: result };
  } catch (error) {
    console.error('导入歌单失败:', error);
    return { success: false, error: error.message };
  }
}
export default {
  search,
  getMediaSource,
  getLyric,
  getAlbumInfo,
  getArtistWorks,
  importMusicSheet,
  getRecommendSheetTags,
  getRecommendSheetsByTag,
  getMusicSheetInfo
};