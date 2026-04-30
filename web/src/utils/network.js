import { Capacitor } from '@capacitor/core';

export const isNativeApp = Capacitor.isNativePlatform();

const requestRules = [
  {
    proxyPrefix: '/musicu',
    upstreamOrigins: ['https://u.y.qq.com/cgi-bin/musicu.fcg'],
    nativeTarget: 'https://u.y.qq.com/cgi-bin/musicu.fcg',
    headers: {
      Referer: 'https://y.qq.com/',
      'X-Requested-With': 'XMLHttpRequest',
    },
  },
  {
    proxyPrefix: '/qqapi',
    upstreamOrigins: ['https://u.y.qq.com'],
    nativeTarget: 'https://u.y.qq.com',
    headers: {
      Referer: 'https://y.qq.com/',
      'X-Requested-With': 'XMLHttpRequest',
    },
  },
  {
    proxyPrefix: '/api',
    upstreamOrigins: ['https://c.y.qq.com', 'http://c.y.qq.com'],
    nativeTarget: 'https://c.y.qq.com',
    headers: {
      Referer: 'https://y.qq.com/',
      'X-Requested-With': 'XMLHttpRequest',
    },
  },
  {
    proxyPrefix: '/qqmusic',
    upstreamOrigins: ['https://u6.y.qq.com'],
    nativeTarget: 'https://u6.y.qq.com',
    headers: {
      Referer: 'https://y.qq.com/',
      'X-Requested-With': 'XMLHttpRequest',
    },
  },
  {
    proxyPrefix: '/qzone',
    upstreamOrigins: ['https://i.y.qq.com', 'http://i.y.qq.com'],
    nativeTarget: 'https://i.y.qq.com',
    headers: {
      Referer: 'https://i.y.qq.com/',
      'X-Requested-With': 'XMLHttpRequest',
    },
  },
  {
    proxyPrefix: '/haitang',
    upstreamOrigins: ['https://musicapi.haitangw.net', 'http://musicapi.haitangw.net'],
    nativeTarget: 'https://musicapi.haitangw.net',
    headers: {
      Referer: 'https://musicapi.haitangw.net/',
    },
  },
  {
    proxyPrefix: '/luoxue',
    upstreamOrigins: ['https://lxmusicapi.onrender.com'],
    nativeTarget: 'https://lxmusicapi.onrender.com',
    headers: {
      Referer: 'https://lxmusicapi.onrender.com/',
    },
  },
];

const isProxyRequest = (url, proxyPrefix) =>
  url === proxyPrefix || url.startsWith(`${proxyPrefix}/`) || url.startsWith(`${proxyPrefix}?`);

const normalizeHeaders = headers => {
  if (!headers) {
    return {};
  }

  if (headers instanceof Headers) {
    return Object.fromEntries(headers.entries());
  }

  if (Array.isArray(headers)) {
    return Object.fromEntries(headers);
  }

  return { ...headers };
};

const findRuleByProxyUrl = url => requestRules.find(rule => isProxyRequest(url, rule.proxyPrefix));

const findRuleByUpstreamUrl = url =>
  requestRules.find(rule => rule.upstreamOrigins.some(origin => url.startsWith(origin)));

export function normalizeRequest(url, headers) {
  if (typeof url !== 'string') {
    return { url, headers: normalizeHeaders(headers) };
  }

  const normalizedHeaders = normalizeHeaders(headers);

  if (isNativeApp) {
    const proxyRule = findRuleByProxyUrl(url);

    if (proxyRule) {
      return {
        url: `${proxyRule.nativeTarget}${url.slice(proxyRule.proxyPrefix.length)}`,
        headers: {
          ...proxyRule.headers,
          ...normalizedHeaders,
        },
      };
    }

    const upstreamRule = findRuleByUpstreamUrl(url);

    if (upstreamRule) {
      const matchedOrigin = upstreamRule.upstreamOrigins.find(origin => url.startsWith(origin));

      return {
        url: `${upstreamRule.nativeTarget}${url.slice(matchedOrigin.length)}`,
        headers: {
          ...upstreamRule.headers,
          ...normalizedHeaders,
        },
      };
    }

    return { url, headers: normalizedHeaders };
  }

  const upstreamRule = findRuleByUpstreamUrl(url);

  if (upstreamRule) {
    const matchedOrigin = upstreamRule.upstreamOrigins.find(origin => url.startsWith(origin));

    return {
      url: `${upstreamRule.proxyPrefix}${url.slice(matchedOrigin.length)}`,
      headers: normalizedHeaders,
    };
  }

  return { url, headers: normalizedHeaders };
}

export function normalizeFetchArgs(url, init = {}) {
  const { url: normalizedUrl, headers } = normalizeRequest(url, init.headers);

  return [
    normalizedUrl,
    {
      ...init,
      headers,
    },
  ];
}
