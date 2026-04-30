# MusicFree Android

Native Android app built with Kotlin and Jetpack Compose.

## Scope

- Four-tab bottom navigation: `首页` / `播放列表` / `收藏` / `我的`
- Native Compose UI with a light mint / aqua visual style based on the provided references
- Search flow mapped to the existing `web` behavior
- QQ top lists, recommended sheets, public sheet detail, lyric loading, and playback source fetching
- Local favorites, search history, player quality, and source preferences
- Mini player + full player view backed by Media3 / ExoPlayer

## Interface Mapping

The Android data layer mirrors the behavior already used in the web project:

- `search` -> `u.y.qq.com/cgi-bin/musicu.fcg`
- `getTopLists` / `getTopListDetail` -> `musicToplist.ToplistInfoServer`
- `getRecommendSheetTags` -> `c.y.qq.com/splcloud/fcgi-bin/fcg_get_diss_tag_conf.fcg`
- `getRecommendSheetsByTag` -> `c.y.qq.com/splcloud/fcgi-bin/fcg_get_diss_by_tag.fcg`
- `getMusicSheetInfo` -> `i.y.qq.com/qzone/fcg-bin/fcg_ucc_getcdinfo_byids_cp.fcg`
- `getLyric` -> `c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg`
- playback source -> `musicapi.haitangw.net` with `lxmusicapi.onrender.com` fallback

## Structure

- `app/src/main/java/com/musicfree/android/data`
  Data models, remote calls, local storage, repository mapping
- `app/src/main/java/com/musicfree/android/player`
  ExoPlayer controller and lyric parser
- `app/src/main/java/com/musicfree/android/ui`
  Navigation, screens, components, theme

## Build

If your machine already has Android SDK installed:

```powershell
$env:ANDROID_HOME="C:\Users\64213\AppData\Local\Android\Sdk"
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
.\gradlew.bat :app:assembleDebug
```

If `gradlew` cannot download the Gradle distribution because of network limits, you can also run the cached local Gradle installation directly after setting the same SDK variables.

## Verified

- `:app:compileDebugKotlin`
- `:app:assembleDebug`

The generated debug APK is placed under:

```text
android/app/build/outputs/apk/debug/app-debug.apk
```
