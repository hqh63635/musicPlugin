// QQ音乐插件API调用示例

// 导入插件包装器
const plugin = require('./plugin-wrapper.js');

// 工具函数：安全输出值，只显示非undefined/非null的值
function safeLog(label, value, indent = '   ') {
    if (value !== undefined && value !== null) {
        console.log(`${indent}${label}: ${value}`);
    }
}

// 工具函数：安全输出对象的多个字段
function safeLogObject(label, obj, fields, indent = '   ') {
    if (obj && typeof obj === 'object') {
        console.log(`${indent}${label}:`);
        fields.forEach(([fieldName, displayName]) => {
            safeLog(displayName, obj[fieldName], `${indent}   `);
        });
    }
}

async function runApiExamples() {
    console.log('=== QQ音乐插件API调用示例 ===\n');

    // 1. 获取插件基本信息
    console.log('1. 插件基本信息:');
    safeLog('平台', plugin.platform);
    safeLog('作者', plugin.author);
    safeLog('版本', plugin.version);
    if (plugin.supportedSearchType && Array.isArray(plugin.supportedSearchType)) {
        safeLog('支持的搜索类型', plugin.supportedSearchType.join(', '));
    }
    const functions = Object.keys(plugin).filter(key => typeof plugin[key] === 'function');
    if (functions.length > 0) {
        safeLog('支持的方法', functions.join(', '));
    }
    console.log();

    // 2. 搜索音乐
    console.log('2. 搜索音乐 (周杰伦):');
    try {
        const searchResults = await plugin.search('周杰伦', 1, 'music');
        if (searchResults) {
            safeLog('搜索结果总数', searchResults.total);
            
            if (searchResults.data && Array.isArray(searchResults.data)) {
                console.log('   前3条结果:', searchResults.data);

                const firstSong = searchResults.data[0];
                if (firstSong) {
                    console.log();

                    // 3. 获取媒体源（播放链接）
                    console.log('3. 获取媒体源 (播放链接):');
                    try {
                        const mediaSource = await plugin.getMediaSource(firstSong.id, firstSong.songmid);
                        if (mediaSource) {
                            console.log(`   歌曲: ${firstSong.title} - ${firstSong.artist}`, firstSong);
                        } else {
                            console.log('   未获取到媒体源信息');
                        }
                    } catch (error) {
                        // 静默处理，不显示错误
                    }
                    console.log();

                    // 4. 获取歌词
                    console.log('4. 获取歌词:');
                    try {
                        const lyric = await plugin.getLyric(firstSong);
                        if (lyric) {
                            console.log(`   歌曲: ${firstSong.title} - ${firstSong.artist}`);
                            console.log('   歌词前几行:', lyric);
                            const lyricLines = lyric.split('\n').filter(line => line.trim());
                            if (lyricLines.length > 0) {
                                lyricLines.slice(0, 3).forEach(line => {
                                    console.log(`      ${line}`);
                                });
                                if (lyricLines.length > 3) {
                                    console.log(`      ... (共${lyricLines.length}行)`);
                                }
                            } else {
                                console.log('      歌词内容为空');
                            }
                        } else {
                            console.log('   未找到歌词');
                        }
                    } catch (error) {
                        // 静默处理，不显示错误
                    }
                    console.log();

                    // 5. 获取专辑信息
                    console.log('5. 获取专辑信息:');
                    try {
                        const albumInfo = await plugin.getAlbumInfo(firstSong.albumid, firstSong.albummid);
                        if (albumInfo) {
                            safeLogObject('   专辑信息', albumInfo, [
                                ['title', '专辑名'],
                                ['artist', '艺术家'],
                                ['publishTime', '发行时间'],
                                ['artwork', '专辑封面'],
                                ['songCount', '歌曲数量']
                            ]);
                            
                            if (albumInfo.songs && Array.isArray(albumInfo.songs) && albumInfo.songs.length > 0) {
                                console.log('   前3首歌曲:');
                                albumInfo.songs.slice(0, 3).forEach((song, index) => {
                                    safeLog(`${index + 1}.`, song.title, '      ');
                                });
                            }
                        }
                    } catch (error) {
                        // 静默处理，不显示错误
                    }
                    console.log();
                }
            }
        }

        // 6. 搜索专辑
        console.log('6. 搜索专辑 (依然范特西):');
        try {
            const albumSearchResults = await plugin.search('依然范特西', 1, 'album');
            if (albumSearchResults && albumSearchResults.data && Array.isArray(albumSearchResults.data)) {
                safeLog('搜索结果总数', albumSearchResults.total);
                
                const album = albumSearchResults.data[0];
                if (album) {
                    safeLogObject('   专辑信息', album, [
                        ['title', '专辑名'],
                        ['artist', '艺术家'],
                        ['artwork', '专辑封面'],
                        ['albumid', '专辑ID'],
                        ['albummid', 'Albummid']
                    ]);
                }
            }
        } catch (error) {
            // 静默处理，不显示错误
        }
        console.log();

        // 7. 搜索艺术家
        console.log('7. 搜索艺术家 (周杰伦):');
        try {
            const artistSearchResults = await plugin.search('周杰伦', 1, 'artist');
            if (artistSearchResults && artistSearchResults.data && Array.isArray(artistSearchResults.data)) {
                safeLog('搜索结果总数', artistSearchResults.total);
                
                const artist = artistSearchResults.data[0];
                if (artist) {
                    safeLogObject('   艺术家信息', artist, [
                        ['name', '艺术家名'],
                        ['id', '艺术家ID'],
                        ['artwork', '艺术家封面']
                    ]);
                }
            }
        } catch (error) {
            // 静默处理，不显示错误
        }
        console.log();

        // 8. 搜索歌单
        console.log('8. 搜索歌单 (热门华语):');
        try {
            const sheetSearchResults = await plugin.search('热门华语', 1, 'sheet');
            if (sheetSearchResults && sheetSearchResults.data && Array.isArray(sheetSearchResults.data)) {
                safeLog('搜索结果总数', sheetSearchResults.total);
                
                const sheet = sheetSearchResults.data[0];
                if (sheet) {

                    console.log('   歌单信息:', sheet);
                }
            }
        } catch (error) {
            // 静默处理，不显示错误
        }
        console.log();

        // 9. 获取排行榜
        console.log('9. 获取排行榜:');
        try {
            const topLists = await plugin.getTopLists();
            if (topLists && Array.isArray(topLists)) {
                console.log('   热门排行榜:', topLists);
    
            }
        } catch (error) {
            // 静默处理，不显示错误
        }
        console.log();

        console.log('=== API调用示例完成 ===');
    } catch (error) {
        console.error('API调用失败:', error);
    }
}

// 运行示例
runApiExamples();
