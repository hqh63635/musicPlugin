// 插件包装器
import CryptoJS from 'crypto-js';
import dayjs from 'dayjs';
import axios from 'axios';
import bigInt from 'big-integer';
import qs from 'qs';
import * as cheerioModule from 'cheerio';
import he from 'he';
import qqJsContent from './qq.js?raw';

// Handle cheerio export structure (common issue with cheerio)
const cheerio = cheerioModule.default || cheerioModule;

// 设置axios默认配置
axios.defaults.timeout = 15000;

// 配置axios请求拦截器，自动代理QQ音乐API请求
axios.interceptors.request.use(
  config => {
    // 如果请求URL包含u.y.qq.com或c.y.qq.com，自动转换为代理路径
    if (config.url.includes('u.y.qq.com/cgi-bin/musicu.fcg')) {
      // 转换为 /musicu 代理路径
      const params = config.url.split('?')[1];
      config.url = `/musicu?${params}`;
    } else if (config.url.includes('u.y.qq.com')) {
      // 转换为 /qqapi 代理路径
      config.url = config.url.replace('https://u.y.qq.com', '/qqapi');
    } else if (config.url.includes('c.y.qq.com')) {
      // 转换为 /api 代理路径，同时处理HTTP和HTTPS协议
      config.url = config.url.replace(/https?:\/\/c\.y\.qq\.com/, '/api');
    } else if (config.url.includes('i.y.qq.com')) {
      // 转换为 /qqapi 代理路径
      config.url = config.url.replace('http://i.y.qq.com', '/qzone');
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 定义可用的包
const packages = {
  cheerio,
  'crypto-js': CryptoJS,
  axios,
  dayjs,
  'big-integer': bigInt,
  qs,
  he,
};

// 模拟 require 函数
const _require = packageName => {
  if (packages[packageName]) {
    packages[packageName].default = packages[packageName];
    return packages[packageName];
  }
  return null;
};

// 原始插件代码通过import qqJsContent from './qq.js?raw'导入

// 定义插件状态码
const PluginStateCode = {
  VersionNotMatch: 'VERSION NOT MATCH',
  CannotParse: 'CANNOT PARSE',
};

// 插件类
class Plugin {
  constructor(funcCode, pluginPath) {
    let _instance;
    const _module = { exports: {}, loaded: false };
    let loadResolveCallback = null;
    const ensurePluginInitialized = new Promise(resolve => {
      loadResolveCallback = resolve;
    });

    try {
      if (typeof funcCode === 'string') {
        // 插件的环境变量
        const env = {
          getUserVariables: () => ({}),
          os: 'browser',
          appVersion: '1.0.0',
          lang: 'zh-CN',
        };

        const _process = {
          platform: 'browser',
          version: '1.0.0',
          env,
          ensurePluginInitialized,
        };

        // 创建插件实例
        _instance = Function(`
                    'use strict';
                    return function(require, __musicfree_require, module, exports, console, env, process) {
                        ${funcCode}
                    }
                `)()(_require, _require, _module, _module.exports, console, env, _process);

        if (_module.exports.default) {
          _instance = _module.exports.default;
        } else {
          _instance = _module.exports;
        }

        loadResolveCallback?.();
      } else {
        _instance = funcCode();
      }

      this.checkValid(_instance);
    } catch (e) {
      console.error('插件加载失败:', e);
      _instance = {
        _path: pluginPath,
        platform: 'qq',
        appVersion: '1.0.0',
        async getMediaSource() {
          return null;
        },
        async search() {
          return {};
        },
        async getAlbumInfo() {
          return null;
        },
        async getPlaylistDetail() {
          return null;
        },
        async getTopList() {
          return null;
        },
        async getLyric() {
          return null;
        },
      };
    }

    this.instance = _instance;
    this.path = pluginPath;
    this.name = _instance.platform || 'qq';
    _module.loaded = true;
  }

  checkValid(_instance) {
    // 可以添加版本检查等逻辑
    return true;
  }
}

// 创建沙箱函数
async function createSandbox() {
  // 创建插件实例
  const plugin = new Plugin(qqJsContent, './qq.js');

  // 返回插件实例
  return plugin.instance;
}

// 创建默认插件实例
const defaultPlugin = new Plugin(qqJsContent, './qq.js');

// 导出默认插件实例和createSandbox函数
export default {
  ...defaultPlugin.instance,
  createSandbox,
};
