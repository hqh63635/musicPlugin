import { createRouter, createWebHistory } from 'vue-router';

// 定义路由组件
// 注意：这里使用了动态导入来实现路由懒加载
const RankList = () => import('../views/RankList.vue');
import Artist from '../views/Artist.vue';
import ArtistDetail from '../views/ArtistDetail.vue';
const Album = () => import('../views/Album.vue');
const Search = () => import('../views/Search.vue');
const Playlist = () => import('../views/Playlist.vue');
const RecentPlay = () => import('../views/RecentPlay.vue');
const Download = () => import('../views/Download.vue');
const FavoriteList = () => import('../views/FavoriteList.vue');
import Layout from '../components/Layout.vue';
const MusicSheetDetail = () => import('../views/MusicSheetDetail.vue');

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/ranklist',
    },
    {
      path: '/ranklist',
      name: 'RankList',
      component: RankList,
    },
    {
      path: '/artist',
      name: 'Artist',
      component: Layout,
      redirect: { name: 'ArtistList' },
      children: [
        {
          path: '',
          name: 'ArtistList',
          component: Artist,
        },
        {
          path: ':id',
          name: 'ArtistDetail',
          component: ArtistDetail,
        },
      ],
    },
    {
      path: '/album',
      name: 'Album',
      component: Layout,
      redirect: { name: 'AlbumList' },
      children: [
        {
          path: '',
          name: 'AlbumList',
          component: Album,
        },
        {
          path: ':id',
          name: 'AlbumDetail',
          component: ArtistDetail,
        },
      ],
    },
    {
      path: '/search',
      name: 'Search',
      component: Search,
      props: true,
    },
    {
      path: '/playlist',
      name: 'Playlist',
      component: Playlist,
    },
    {
      path: '/recent',
      name: 'Recent',
      component: RecentPlay,
    },
    {
      path: '/download',
      name: 'Download',
      component: Download,
    },
    {
      path: '/favorite',
      name: 'FavoriteList',
      component: FavoriteList,
    },
    {
      path: '/musicsheet/:id',
      name: 'MusicSheetDetail',
      component: MusicSheetDetail,
    },
  ],
});

export default router;
