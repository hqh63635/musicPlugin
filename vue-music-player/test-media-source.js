import { getMediaSource } from './src/services/api.js';

// 测试获取媒体资源
getMediaSource('0039MnYb0qxYhV', 'standard')
  .then(result => {
    console.log('获取媒体资源成功:', result);
  })
  .catch(error => {
    console.error('获取媒体资源失败:', error);
  });
