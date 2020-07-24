package li.lingfeng.ltsystem.prefs;

import android.os.RemoteException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import li.lingfeng.ltsystem.ILTPrefListener;
import li.lingfeng.ltsystem.LTPref;

public class Prefs {

    // System properties read write.
    private static PreferenceStore _instance = new PreferenceStore("persist.sys.ltweaks.");
    public static PreferenceStore instance() {
        return _instance;
    }

    public static final String LARGE_STORE_PATH = "/data/system/ltweaks_large_store";

    // Large store read write.
    private static LargeStore _large = new LargeStore();
    public static LargeStore large() {
        return _large;
    }

    public static class LargeStore {

        private Map<String, Object> mValues = new HashMap<>();

        public List<String> getStringList(int key, List<String> defValue) {
            return getStringList(getKeyById(key), defValue, true);
        }

        /**
         * @param listenForCache false if you add listener from outside and handle value updated by yourself, otherwise true.
         */
        public List<String> getStringList(int key, List<String> defValue, boolean listenForCache) {
            return getStringList(getKeyById(key), defValue, listenForCache);
        }

        public List<String> getStringList(String key, List<String> defValue, boolean listenForCache) {
            List<String> result;
            if (listenForCache) {
                result = (List<String>) mValues.get(key);
                if (result == null) {
                    result = LTPref.instance().getStringList(key);
                    if (result == null) {
                        result = new ArrayList<>();
                    }
                    mValues.put(key, result);
                    addListener(key, new ILTPrefListener.Stub() {
                        @Override
                        public void onPrefChanged(String key) throws RemoteException {
                            mValues.remove(key);
                            removeListener(key, this);
                        }
                    });
                }
            } else {
                result = LTPref.instance().getStringList(key);
            }
            return result != null && result.size() > 0 ? result : defValue;
        }

        public void putStringList(int key, String[] value) {
            putStringList(getKeyById(key), Arrays.asList(value));
        }

        public void putStringList(int key, List<String> value) {
            putStringList(getKeyById(key), value);
        }

        public void putStringList(String key, List<String> value) {
            LTPref.instance().putStringList(key, value);
        }

        public void appendStringToList(int key, String value, int limit) {
            appendStringToList(getKeyById(key), value, limit);
        }

        public void appendStringToList(String key, String value, int limit) {
            LTPref.instance().appendStringToList(key, value, limit);
        }

        public void addListener(int key, ILTPrefListener listener) {
            addListener(getKeyById(key), listener);
        }

        public void addListener(String key, ILTPrefListener listener) {
            LTPref.instance().addListener(key, listener);
        }

        public void removeListener(int key, ILTPrefListener listener) {
            removeListener(getKeyById(key), listener);
        }

        public void removeListener(String key, ILTPrefListener listener) {
            LTPref.instance().removeListener(key, listener);
        }

        protected String getKeyById(int id) {
            return PrefKeys.getById(id);
        }
    }
}
