const qualityLevels = {
  low: 'standard',
  standard: 'exhigh',
  high: 'exhigh',
  exhigh: 'lossless',
  super: 'lossless',
};

export async function getMediaSource(musicItem, quality = 'standard') {
  if (!musicItem.songmid) throw new Error('长青音源缺少 QQ 歌曲 songmid');
  const params = new URLSearchParams({
    type: 'mp3',
    id: musicItem.songmid,
    level: qualityLevels[quality] || qualityLevels.standard,
  });
  const response = await fetch(`/changqing-resolve?${params}`, {
    signal: AbortSignal.timeout(20000),
  });
  if (!response.ok) throw new Error('长青音源获取失败');
  if (!(response.headers.get('content-type') || '').includes('application/json')) {
    throw new Error('长青解析接口未返回 JSON，请检查部署站点的 /changqing-resolve Nginx 配置并重载');
  }
  const data = await response.json();
  if (!/^https?:\/\//.test(data.url || '')) throw new Error('长青未返回真实播放地址');
  return { url: data.url };
}

export default { getMediaSource };
