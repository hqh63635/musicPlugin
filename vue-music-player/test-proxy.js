// 测试代理配置是否正常工作
import axios from 'axios'

async function testQQMusicProxy() {
  try {
    // 测试搜索API（不使用jsonpAdapter，直接处理JSONP响应）
    console.log('测试搜索API...')
    const searchResponse = await axios.get('http://localhost:5173/api/soso/fcgi-bin/client_search_cp', {
      params: {
        w: '周杰伦',
        p: 1,
        n: 10,
        g_tk: 5381,
        loginUin: 0,
        hostUin: 0,
        inCharset: 'utf8',
        outCharset: 'utf-8',
        notice: 0,
        platform: 'yqq',
        needNewCode: 0,
        jsonpCallback: 'jsonpCallback'
      },
      headers: {
        'Referer': 'https://y.qq.com/',
        'Accept': '*/*'
      }
    })
    
    console.log('✓ 搜索API代理测试成功！')
    console.log('响应状态:', searchResponse.status)
    console.log('响应数据:', searchResponse.data)
    
    console.log('\n🎉 代理测试通过！')
  } catch (error) {
    console.error('❌ 代理测试失败:', error.message)
    if (error.response) {
      console.error('响应状态:', error.response.status)
      console.error('响应数据:', error.response.data)
    } else if (error.request) {
      console.error('没有收到响应:', error.request)
    } else {
      console.error('请求配置错误:', error.config)
    }
  }
}

testQQMusicProxy()