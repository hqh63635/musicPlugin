import { createRouter, createWebHistory } from 'vue-router'

// 定义路由组件
// 注意：这里使用了动态导入来实现路由懒加载
const Recommend = () => import('../views/Recommend.vue')
const RankList = () => import('../views/RankList.vue')
const Artist = () => import('../views/Artist.vue')
const Album = () => import('../views/Album.vue')
const Search = () => import('../views/Search.vue')

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Recommend',
      component: Recommend
    },
    {
      path: '/ranklist',
      name: 'RankList',
      component: RankList
    },
    {
      path: '/artist',
      name: 'Artist',
      component: Artist
    },
    {
      path: '/album',
      name: 'Album',
      component: Album
    },
    {
      path: '/search',
      name: 'Search',
      component: Search,
      props: true
    }
  ]
})

export default router
