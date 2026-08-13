import axios from 'axios';

const qualityLevels = {
  low: '128k',
  standard: '320k',
  high: '320k',
  exhigh: 'flac',
  super: 'flac',
};

export async function getMediaSource(musicItem, quality) {
  const response = await axios.get('/zddyr/lx/api/', {
    params: {
      source: 'qq',
      songmid: musicItem.songmid,
      quality: qualityLevels[quality] || qualityLevels.standard,
    },
  });

  const data = response.data;
  if (data?.code !== 200 || !data?.url) {
    throw new Error(data?.message || 'ZDDYR API 未返回播放地址');
  }

  return { url: data.url.replace(/^http:\/\//, 'https://') };
}

export default {
  getMediaSource,
};
