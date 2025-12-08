import axios from 'axios';

// 测试getTopList API
async function testTopList() {
  try {
    const response = await axios.get('http://localhost:5174/api/v8/fcg-bin/fcg_myqq_toplist.fcg', {
      params: {
        format: 'json',
        g_tk: 5381,
        loginUin: 0,
        hostUin: 0,
        inCharset: 'utf8',
        outCharset: 'utf-8',
        notice: 0,
        platform: 'yqq',
        needNewCode: 0,
        _: Date.now()
      },
      headers: {
        'Referer': 'https://y.qq.com/'
      }
    });
    
    console.log('getTopList API测试结果:');
    console.log('状态码:', response.status);
    console.log('是否成功:', response.data.code === 0);
    console.log('返回数据数量:', response.data.data.topList ? response.data.data.topList.length : '0');
    
    if (response.data.code === 0 && response.data.data.topList) {
      console.log('前5个歌单:');
      response.data.data.topList.slice(0, 5).forEach((item, index) => {
        console.log(`${index + 1}. ${item.topTitle}`);
      });
    }
    
    return response.data;
  } catch (error) {
    console.error('getTopList API测试失败:', error.message);
    if (error.response) {
      console.error('响应状态:', error.response.status);
      console.error('响应数据:', JSON.stringify(error.response.data, null, 2));
    }
    return null;
  }
}

// 测试getRecommendAlbums API
async function testRecommendAlbums() {
  try {
    const response = await axios.get('http://localhost:5174/api/v8/fcg-bin/fcg_v8_toplist_cp.fcg', {
      params: {
        g_tk: 5381,
        loginUin: 0,
        hostUin: 0,
        inCharset: 'utf8',
        outCharset: 'utf-8',
        notice: 0,
        platform: 'yqq',
        needNewCode: 0,
        _: Date.now()
      },
      headers: {
        'Referer': 'https://y.qq.com/'
      }
    });
    
    console.log('\ngetRecommendAlbums API测试结果:');
    console.log('状态码:', response.status);
    console.log('是否成功:', response.data.code === 0);
    console.log('完整返回数据:', JSON.stringify(response.data, null, 2));
    
    return response.data;
  } catch (error) {
    console.error('getRecommendAlbums API测试失败:', error.message);
    if (error.response) {
      console.error('响应状态:', error.response.status);
      console.error('响应数据:', JSON.stringify(error.response.data, null, 2));
    }
    return null;
  }
}

// 执行测试
async function runTests() {
  await testTopList();
  // await testRecommendAlbums();
}

runTests();