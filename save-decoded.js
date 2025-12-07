const fs = require('fs');
const vm = require('vm');

// 导入预定义的库
const CryptoJs = require('crypto-js');
const dayjs = require('dayjs');
const axios = require('axios');
const bigInt = require('big-integer');
const qs = require('qs');
const cheerio = require('cheerio');
const he = require('he');

// 读取加密的qq.js文件
const encryptedCode = fs.readFileSync('./qq.js', 'utf8');

// 创建与plugin.ts中相同的包映射
const packages = {
    cheerio,
    "crypto-js": CryptoJs,
    axios,
    dayjs,
    "big-integer": bigInt,
    qs,
    he
};

// 模拟_require函数
const _require = (packageName) => {
    const pkg = packages[packageName];
    if (pkg) {
        pkg.default = pkg;
        return pkg;
    }
    console.log(`未找到包: ${packageName}`);
    return null;
};

// 模拟环境变量
const env = {
    getUserVariables: () => ({}),
    os: process.platform,
    appVersion: '1.0.0',
    lang: 'zh-CN'
};

// 模拟process对象
const mockProcess = {
    platform: process.platform,
    version: '1.0.0',
    env,
    ensurePluginInitialized: Promise.resolve()
};

// 创建沙箱环境
const sandbox = {
    console: console,
    require: _require,
    process: mockProcess,
    module: { exports: {} },
    exports: {},
    global: {},
    __filename: './qq.js',
    __dirname: './'
};

// 执行解密代码
const context = vm.createContext(sandbox);
vm.runInContext(encryptedCode, context);

// 获取解密后的插件对象
const plugin = context.module.exports;

// 将插件对象转换为字符串表示（包括函数）
const pluginStr = JSON.stringify(plugin, (key, value) => {
    if (typeof value === 'function') {
        return value.toString();
    }
    return value;
}, 2);

// 保存为可读的JavaScript文件
const jsCode = `module.exports = ${pluginStr};`;
fs.writeFileSync('./qq-decoded.js', jsCode);
console.log('解密后的插件已保存到 qq-decoded.js');

// 保存为JSON文件（用于查看结构）
fs.writeFileSync('./qq-decoded.json', pluginStr);
console.log('解密后的插件结构已保存到 qq-decoded.json');
