// 测试api.js中的所有方法
const path = require('path');

// 由于api.js使用ES模块语法，我们需要使用动态导入
async function testApi() {
  try {
    // 动态导入api.js
    const api = await import('./vue-music-player/src/services/api.js');
    
    // 调用getTopLists方法
    console.log('测试getTopLists方法...');
    const topListsResult = await api.default.getTopLists();
    console.log('getTopLists结果:', JSON.stringify(topListsResult, null, 2));
    
    // 测试search方法
    console.log('\n测试search方法...');
    const searchResult = await api.default.search('周杰伦');
    console.log('search结果:', JSON.stringify(searchResult, null, 2));
    
    // 如果有搜索结果，测试getMediaSource和getLyric方法
    if (searchResult.success && searchResult.data.songs && searchResult.data.songs.length > 0) {
      const song = searchResult.data.songs[0];
      console.log('\n测试getMediaSource方法...');
      const mediaSourceResult = await api.default.getMediaSource(song.id, song.songmid || song.id);
      console.log('getMediaSource结果:', JSON.stringify(mediaSourceResult, null, 2));
      
      console.log('\n测试getLyric方法...');
      const lyricResult = await api.default.getLyric(song.id, song.songmid || song.id);
      console.log('getLyric结果:', JSON.stringify(lyricResult, null, 2));
    }
    
    // 测试getAlbumInfo方法
    console.log('\n测试getAlbumInfo方法...');
    const albumResult = await api.default.getAlbumInfo('002J4UUk29y8BY');
    console.log('getAlbumInfo结果:', JSON.stringify(albumResult, null, 2));
    
    // 测试getPlaylistDetail方法
    console.log('\n测试getPlaylistDetail方法...');
    const playlistResult = await api.default.getPlaylistDetail('3050083492');
    console.log('getPlaylistDetail结果:', JSON.stringify(playlistResult, null, 2));
    
    // 测试getArtistSongs方法
    console.log('\n测试getArtistSongs方法...');
    const artistSongsResult = await api.default.getArtistSongs('6452');
    console.log('getArtistSongs结果:', JSON.stringify(artistSongsResult, null, 2));
    
  } catch (error) {
    console.error('测试失败:', error);
  }
}

// 运行测试
testApi();
