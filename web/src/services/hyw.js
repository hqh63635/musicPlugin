import axios from 'axios';

const qualityLevels = {
  low: '128k',
  standard: '320k',
  high: '320k',
  exhigh: 'flac',
  super: 'flac24bit',
};

export async function getMediaSource(musicItem, quality) {
  const response = await axios.get('/api/music/url', {
    params: {
      source: 'tx',
      songmid: musicItem.songmid,
      quality: qualityLevels[quality] || qualityLevels.standard,
    },
  });
  const data = response.data;
  if (data?.code !== 200 || !data?.url) {
    throw new Error(data?.message || 'HYWmusic API 未返回播放地址');
  }
  return { url: data.url.replace(/^http:\/\//, 'https://') };
}

export default { getMediaSource };
