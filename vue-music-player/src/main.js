import { createApp } from 'vue';
import { createPinia } from 'pinia';
import AntDesign from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';
import piniaPersist from 'pinia-plugin-persistedstate';
import './style.css';
import App from './App.vue';
import router from './router';

const app = createApp(App);
const pinia = createPinia();
// 正确安装pinia持久化插件
pinia.use(piniaPersist);

app.use(pinia);
app.use(router);
app.use(AntDesign);
app.mount('#app');
