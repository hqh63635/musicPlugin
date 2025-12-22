import { createApp } from 'vue';
import { createPinia } from 'pinia';
import AntDesign from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';
import piniaPersist from 'pinia-plugin-persistedstate';
import VxeUIBase from 'vxe-pc-ui';
import 'vxe-pc-ui/es/style.css';

import VxeUITable from 'vxe-table';
import 'vxe-table/es/style.css';
import './style.css';
import './assets/css/index.css';
import './styles/theme.css';
import './styles/common.css';

import App from './App.vue';
import router from './router';
import i18n from './locales'; // 导入i18n实例

const app = createApp(App);
const pinia = createPinia();
// 正确安装pinia持久化插件
pinia.use(piniaPersist);

app.use(pinia);
app.use(router);
app.use(i18n); // 使用i18n
app.use(AntDesign);
app.use(VxeUIBase);
app.use(VxeUITable);
app.mount('#app');