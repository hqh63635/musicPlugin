const qualityFields={low:'song_play_url_standard',standard:'song_play_url_standard',high:'song_play_url_hq',exhigh:'song_play_url_hq',super:'song_play_url_sq'};
export async function getMediaSource(song,quality='standard'){
 if(!song.songmid) throw new Error('cihedai 音源缺少 songmid');
 const r=await fetch(`/cihedai-api?mid=${encodeURIComponent(song.songmid)}`,{signal:AbortSignal.timeout(15000)});
 if(!r.ok) throw new Error(`cihedai 请求失败 (${r.status})`);
 const d=await r.json(),u=d[qualityFields[quality]||qualityFields.standard];
 if(!/^https?:\/\/\S+$/i.test(u||'')) throw new Error('cihedai 未返回播放地址');
 return {url:u.replace(/^http:/i,'https:')};
}
export default {getMediaSource};
