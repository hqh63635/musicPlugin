import IdentificationIcon from '@/assets/icons/identification.svg';
import TrophyIcon from '@/assets/icons/trophy.svg';
import HeartIcon from '@/assets/icons/heart.svg';
import ClockIcon from '@/assets/icons/clock.svg';
import ArrayDownloadTrayIcon from '@/assets/icons/array-download-tray.svg';
import ListBulletIcon from '@/assets/icons/list-bullet.svg';
import AlbumIcon from '@/assets/icons/album.svg';
import UserIcon from '@/assets/icons/user.svg';

export const appNavItems = [
  { key: 'search', icon: IdentificationIcon, text: 'sidebar.search', path: '/search' },
  { key: 'ranklist', icon: TrophyIcon, text: 'sidebar.ranklist', path: '/ranklist' },
  { key: 'favorite', icon: HeartIcon, text: 'sidebar.favorite', path: '/favorite' },
  { key: 'recent', icon: ClockIcon, text: 'sidebar.recent', path: '/recent' },
  {
    key: 'download',
    icon: ArrayDownloadTrayIcon,
    text: 'sidebar.download',
    path: '/download',
  },
  { key: 'playlist', icon: ListBulletIcon, text: 'sidebar.playlist', path: '/playlist' },
  { key: 'album', icon: AlbumIcon, text: 'sidebar.album', path: '/album' },
  { key: 'artist', icon: UserIcon, text: 'sidebar.artist', path: '/artist' },
];

export const mobileNavItems = appNavItems.filter(item =>
  ['ranklist', 'search', 'favorite', 'playlist', 'artist'].includes(item.key)
);
