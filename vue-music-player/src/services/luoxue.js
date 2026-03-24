import axios from 'axios';

const qualityLevels = {
  low: '128k',
  standard: '320k',
  high: '320k',
  super: '320k',
};

export async function getMediaSource(musicItem, quality) {
  const res = (
    await axios.get(`/luoxue/url/tx/${musicItem.songmid}/${qualityLevels[quality]}`, {
      headers: {
        'X-Request-Key': 'share-v3',
      },
    })
  ).data;
  return {
    url: res.url,
  };
}

export default {
  getMediaSource,
};
