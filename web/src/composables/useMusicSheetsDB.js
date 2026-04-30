import { ref } from 'vue';

const DB_NAME = 'MusicSheetsDB';
const DB_VERSION = 1;
const STORE_NAME = 'sheets';

/**
 * 初始化数据库连接
 */
const initDB = () =>
  new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION);

    request.onupgradeneeded = e => {
      const db = e.target.result;
      if (!db.objectStoreNames.contains(STORE_NAME)) {
        db.createObjectStore(STORE_NAME, {
          keyPath: 'id',
          autoIncrement: true,
        });
      }
    };

    request.onsuccess = e => resolve(e.target.result);
    request.onerror = () => reject(request.error);
  });

/**
 * 获取所有歌单
 */
export const getAllSheets = async () => {
  const db = await initDB();
  const list = [];

  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_NAME, 'readonly');
    const store = tx.objectStore(STORE_NAME);

    store.openCursor().onsuccess = e => {
      const cursor = e.target.result;
      if (cursor) {
        list.push(cursor.value);
        cursor.continue();
      }
    };

    tx.oncomplete = () => {
      db.close();
      resolve(list);
    };
    tx.onerror = () => reject(tx.error);
  });
};

/**
 * 新增/更新歌单
 * @param {Object} sheet - 歌单对象
 */
export const saveSheet = async sheet => {
  const db = await initDB();

  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_NAME, 'readwrite');
    const store = tx.objectStore(STORE_NAME);

    // 对歌单对象进行JSON序列化处理，确保没有无法克隆的属性
    const serializedSheet = JSON.parse(JSON.stringify(sheet));

    const req = serializedSheet.id
      ? store.put(serializedSheet) // 更新
      : store.add(serializedSheet); // 新增

    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error);

    tx.oncomplete = () => {
      db.close();
      resolve();
    };
  });
};

/**
 * 向已有歌单追加歌曲（自动去重）
 * @param {number} sheetId - 歌单ID
 * @param {Array} songs - 歌曲列表
 */
export const addSongsToSheet = async (sheetId, songs) => {
  const db = await initDB();

  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_NAME, 'readwrite');
    const store = tx.objectStore(STORE_NAME);

    const getReq = store.get(Number(sheetId));

    getReq.onsuccess = () => {
      const sheet = getReq.result;
      if (!sheet) return reject('歌单不存在');

      const existIds = new Set((sheet.musicList || []).map(i => i.id));

      // 对歌曲对象进行序列化处理，确保只包含基本数据类型
      const newSongs = songs
        .filter(i => !existIds.has(i.id))
        .map(song => {
          // 使用 JSON 序列化/反序列化去除无法克隆的属性
          try {
            return JSON.parse(JSON.stringify(song));
          } catch (error) {
            console.error('歌曲对象序列化失败:', error);
            // 如果序列化失败，尝试手动提取基本属性
            return {
              id: song.id,
              songmid: song.songmid,
              title: song.title,
              artist: song.artist,
              artwork: song.artwork,
              album: song.album,
              albumid: song.albumid,
              albummid: song.albummid
            };
          }
        });

      // 创建一个新的歌单对象，只包含需要的属性，确保可以被正确序列化
      const updatedSheet = {
        ...sheet,
        musicList: [...(sheet.musicList || []), ...newSongs],
        trackCount: (sheet.musicList || []).length + newSongs.length
      };

      // 对整个歌单对象进行JSON序列化处理，确保没有无法克隆的属性
      const serializedSheet = JSON.parse(JSON.stringify(updatedSheet));

      store.put(serializedSheet);
      resolve();
    };

    getReq.onerror = () => reject(getReq.error);
    tx.oncomplete = () => db.close();
  });
};

/**
 * 获取单个歌单
 * @param {number} sheetId - 歌单ID
 */
export const getSheetById = async sheetId => {
  const db = await initDB();

  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_NAME, 'readonly');
    const store = tx.objectStore(STORE_NAME);
    const req = store.get(Number(sheetId));

    req.onsuccess = () => {
      db.close();
      resolve(req.result);
    };

    req.onerror = () => {
      db.close();
      reject(req.error);
    };
  });
};

/**
 * 删除歌单
 * @param {number} sheetId - 歌单ID
 */
export const deleteSheet = async sheetId => {
  const db = await initDB();

  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_NAME, 'readwrite');
    const store = tx.objectStore(STORE_NAME);
    const req = store.delete(Number(sheetId));

    req.onsuccess = () => {
      db.close();
      resolve();
    };

    req.onerror = () => {
      db.close();
      reject(req.error);
    };
  });
};

/**
 * 清空歌单歌曲
 * @param {number} sheetId - 歌单ID
 */
export const clearSheetSongs = async sheetId => {
  const db = await initDB();

  return new Promise((resolve, reject) => {
    const tx = db.transaction(STORE_NAME, 'readwrite');
    const store = tx.objectStore(STORE_NAME);
    const getReq = store.get(Number(sheetId));

    getReq.onsuccess = () => {
      const sheet = getReq.result;
      if (!sheet) return reject('歌单不存在');

      // 创建一个新的歌单对象，清空歌曲列表
      const updatedSheet = {
        ...sheet,
        musicList: [],
        trackCount: 0
      };

      // 对整个歌单对象进行JSON序列化处理，确保没有无法克隆的属性
      const serializedSheet = JSON.parse(JSON.stringify(updatedSheet));

      store.put(serializedSheet);
      resolve();
    };

    getReq.onerror = () => reject(getReq.error);
    tx.oncomplete = () => db.close();
  });
};

/**
 * MusicSheetsDB 主 Hook
 */
export const useMusicSheetsDB = () => {
  const isLoading = ref(false);
  const error = ref(null);

  /**
   * 安全执行数据库操作，处理加载状态和错误
   * @param {Function} operation - 数据库操作函数
   */
  const executeOperation = async operation => {
    isLoading.value = true;
    error.value = null;

    try {
      const result = await operation();
      return result;
    } catch (err) {
      error.value = err instanceof Error ? err.message : String(err);
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  return {
    // 状态
    isLoading,
    error,

    // 数据库操作方法
    getAllSheets: () => executeOperation(getAllSheets),
    saveSheet: sheet => executeOperation(() => saveSheet(sheet)),
    addSongsToSheet: (sheetId, songs) => executeOperation(() => addSongsToSheet(sheetId, songs)),
    getSheetById: sheetId => executeOperation(() => getSheetById(sheetId)),
    deleteSheet: sheetId => executeOperation(() => deleteSheet(sheetId)),
    clearSheetSongs: sheetId => executeOperation(() => clearSheetSongs(sheetId)),
  };
};

export default useMusicSheetsDB;
