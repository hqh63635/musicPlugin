// 插件包装器
const fs = require('fs');
const path = require('path');
const CryptoJS = require('crypto-js');
const dayjs = require('dayjs');
const axios = require('axios');
const bigInt = require('big-integer');
const qs = require('qs');
const cheerio = require('cheerio');
const he = require('he');

axios.defaults.timeout = 15000;

axios.interceptors.request.use(req => {
  console.log("📡 Axios Request:", req.url)
  console.log("📤 Data:", req.data)
  console.log("📤 Params:", req.params)
  return req
})

axios.interceptors.response.use(res => {
  console.log("📥 Axios Response:", res.config.url)
  console.log("📥 Status:", res.status)
  console.log("📥 Data:", res.data)
  return res
})

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
const _require = (packageName) => {
    if (packages[packageName]) {
        packages[packageName].default = packages[packageName];
        return packages[packageName];
    }
    return null;
};

// 读取原始插件代码
const qqJsContent = fs.readFileSync(path.join(__dirname, 'qq.js'), 'utf8');

// 定义插件状态码
const PluginStateCode = {
    VersionNotMatch: "VERSION NOT MATCH",
    CannotParse: "CANNOT PARSE",
};

// 插件类
class Plugin {
    constructor(funcCode, pluginPath) {
        let _instance;
        const _module = { exports: {}, loaded: false };
        let loadResolveCallback = null;
        const ensurePluginInitialized = new Promise((resolve) => {
            loadResolveCallback = resolve;
        });
        
        try {
            if (typeof funcCode === "string") {
                // 插件的环境变量
                const env = {
                    getUserVariables: () => ({}),
                    os: process.platform,
                    appVersion: '1.0.0',
                    lang: 'zh-CN',
                };
                
                const _process = {
                    platform: process.platform,
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
                `)()(
                    _require,
                    _require,
                    _module,
                    _module.exports,
                    console,
                    env,
                    _process,
                );
                
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
                async getMediaSource() { return null; },
                async search() { return {}; },
                async getAlbumInfo() { return null; },
                async getPlaylistDetail() { return null; },
                async getTopList() { return null; },
                async getLyric() { return null; },
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
    // 读取插件代码
    const pluginContent = fs.readFileSync(path.join(__dirname, 'qq.js'), 'utf8');
    
    // 创建插件实例
    const plugin = new Plugin(pluginContent, './qq.js');
    
    // 返回插件实例
    return plugin.instance;
}

// 创建默认插件实例
const defaultPlugin = new Plugin(qqJsContent, './qq.js');

// 导出默认插件实例和createSandbox函数
module.exports = {
    ...defaultPlugin.instance,
    createSandbox
};
