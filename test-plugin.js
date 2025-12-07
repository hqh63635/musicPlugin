// 测试生成的插件
const plugin = require('./plugin-wrapper.js');

console.log('插件信息:');
console.log('- 平台:', plugin.platform);
console.log('- 作者:', plugin.author);
console.log('- 版本:', plugin.version);
console.log('- 支持的搜索类型:', plugin.supportedSearchType);

// 测试搜索功能
console.log('\n测试搜索功能...');
plugin.search('陈', 1, 'music')
  .then(result => {
    console.log('搜索结果:', result);
  })
  .catch(error => {
    console.error('搜索失败:', error.message);
    console.log('这可能是预期行为，因为网络请求可能失败或需要登录');
  });
