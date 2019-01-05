package li.lingfeng.ltsystem.prefs;

import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.io.FileUtils;

import java.io.File;

import li.lingfeng.ltsystem.LTHelper;
import li.lingfeng.ltsystem.utils.Logger;

public class Prefs {

    // System properties read write.
    private static PreferenceStore _instance = new PreferenceStore("persist.sys.ltweaks.");
    public static PreferenceStore instance() {
        return _instance;
    }

    public static final String LARGE_STORE_GET = Prefs.class.getName() + ".LARGE_STORE_GET";
    public static final String LARGE_STORE_ALL = Prefs.class.getName() + ".LARGE_STORE_ALL";
    public static final String LARGE_STORE_UPDATE = Prefs.class.getName() + ".LARGE_STORE_UPDATE";
    public static final String LARGE_STORE_PATH = "/data/system/ltweaks_large_store";

    // Large store read from tweaks.
    private static LargeStore _large = new LargeStore();
    public static LargeStore large() {
        return _large;
    }

    // Large store read write in preference activity.
    private static LargeStoreEditor _largeEditor;
    public static LargeStoreEditor largeEditor() {
        if (_largeEditor == null) {
            _largeEditor = new LargeStoreEditor();
        }
        return _largeEditor;
    }

    public static class LargeStore {

        protected JSONObject jLargeStore;

        // Load in zygote, so any modification should take effect after reboot.
        public void load() {
            File file = new File(LARGE_STORE_PATH);
            if (file.exists()) {
                try {
                    String content = FileUtils.readFileToString(file, "UTF-8");
                    jLargeStore = JSON.parseObject(content);
                } catch (Throwable e) {
                    Logger.e("Load large store " + LARGE_STORE_PATH + " exception.", e);
                    jLargeStore = new JSONObject();
                }
            } else {
                jLargeStore = new JSONObject();
            }
        }

        // Only set in preference activity, to receive newest large store values.
        public void setLargeStore(JSONObject jLargeStore) {
            this.jLargeStore = jLargeStore;
        }

        public JSONArray getArray(int key, JSONArray jDefArray) {
            return getArray(getKeyById(key), jDefArray);
        }

        public JSONArray getArray(String key, JSONArray jDefArray) {
            JSONArray jArray = jLargeStore.getJSONArray(key);
            return jArray != null ? jArray : jDefArray;
        }

        protected String getKeyById(int id) {
            return PrefKeys.getById(id);
        }
    }

    public static class LargeStoreEditor extends LargeStore {

        public void putArray(int key, JSONArray jArray) {
            putArray(getKeyById(key), jArray);
        }

        public void putArray(String key, JSONArray jArray) {
            sendLargeStoreUpdate(key, jArray.toString());
            jLargeStore.put(key, jArray);
        }

        private void sendLargeStoreUpdate(String key, String value) {
            Intent intent = new Intent(LARGE_STORE_UPDATE);
            intent.putExtra("key", key);
            intent.putExtra("value", value);
            LTHelper.currentApplication().sendBroadcast(intent);
        }
    }
}
