const fs = require('fs');

// 导入预定义的库（与plugin.ts中相同）
const CryptoJs = require('crypto-js');
const dayjs = require('dayjs');
const axios = require('axios');
const bigInt = require('big-integer');
const qs = require('qs');
const cheerio = require('cheerio');
const he = require('he');

// 读取加密的qq.js文件
const encryptedCode = fs.readFileSync('./qq.js', 'utf8');

// 分析加密代码结构
console.log('文件大小:', encryptedCode.length, '字节');
console.log('前500个字符:');
console.log(encryptedCode.substring(0, 500));

// 尝试找到解密函数
const decodeFunctionMatch = encryptedCode.match(/var\s+_0x[\w]+\s*=\s*function/);
if (decodeFunctionMatch) {
    console.log('\n找到解密函数:');
    console.log(decodeFunctionMatch[0]);
}

// 尝试执行解密代码
console.log('\n尝试执行解密...');
try {
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

    // 创建一个完整的Node.js模块环境
    const vm = require('vm');
    const sandbox = {
        console: console,
        require: _require, // 使用模拟的_require
        process: mockProcess,
        module: { exports: {} },
        exports: {},
        global: {},
        __filename: './qq.js',
        __dirname: './'
    };
    
    // 尝试执行代码并捕获结果
    const context = vm.createContext(sandbox);
    vm.runInContext(encryptedCode, context);
    
    // 查找可能的导出对象
    console.log('\n模块导出:', context.module.exports);
    
    // 查找所有对象
    for (const key in context) {
        if (typeof context[key] === 'object' && context[key] !== null) {
            console.log(`\n找到对象: ${key}`);
            console.log('类型:', typeof context[key]);
            
            // 检查是否是插件对象（具有search、getMediaSource等方法）
            const methods = ['search', 'getMediaSource', 'getLyric', 'getAlbumInfo', 'getMusicSheetInfo'];
            const hasPluginMethods = methods.some(method => typeof context[key][method] === 'function');
            
            if (hasPluginMethods) {
                console.log('\n找到可能的插件对象:', key);
                console.log('插件方法:', methods.filter(method => typeof context[key][method] === 'function'));
                
                // 保存解密后的插件对象
                fs.writeFileSync('./decoded-plugin.json', JSON.stringify(context[key], (key, value) => {
                    if (typeof value === 'function') {
                        return value.toString();
                    }
                    return value;
                }, 2));
                console.log('解密后的插件对象已保存到 decoded-plugin.json');
                
                // 保存完整的上下文
                fs.writeFileSync('./decoded-context.json', JSON.stringify(context, (key, value) => {
                    if (typeof value === 'function') {
                        return value.toString();
                    }
                    return value;
                }, 2));
                console.log('完整上下文已保存到 decoded-context.json');
                
                // 保存解密后的代码
                const decodedCode = context.module.exports ? JSON.stringify(context.module.exports, null, 2) : '未找到导出对象';
                fs.writeFileSync('./decoded-code.js', 'module.exports = ' + decodedCode);
                console.log('解密后的代码已保存到 decoded-code.js');
            }
        }
    }
} catch (error) {
    console.log('执行错误:', error.message);
    console.log('错误堆栈:', error.stack);
    
    // 保存错误信息
    fs.writeFileSync('./decode-error.txt', error.message + '\n\n' + error.stack);
    console.log('错误信息已保存到 decode-error.txt');
}
