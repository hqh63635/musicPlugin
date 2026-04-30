import { createI18n } from 'vue-i18n';
import zhCN from './lang/zh-CN.json';
import enUS from './lang/en-US.json';

// 创建i18n实例
const i18n = createI18n({
  locale: 'zh-CN',
  legacy: false, // ⭐⭐⭐ 关键
  globalInjection: true, // ⭐ 让模板里还能用 $t
  fallbackLocale: 'en-US',
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS,
  },
});

export default i18n;
