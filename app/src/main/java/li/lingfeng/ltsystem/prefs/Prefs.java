package li.lingfeng.ltsystem.prefs;

import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.Set;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;

public class Prefs {

    private static PreferenceStore _instance = new Store("persist.sys.ltweaks.");
    public static PreferenceStore instance() {
        return _instance;
    }

    // Remote preference is for large store, due to limitation of SystemProperties value size, read only.
    private static RemotePreference _remote;
    public static RemotePreference remote() {
        if (_remote == null) {
            _remote = new RemotePreference(ILTweaks.currentApplication(),
                    "li.lingfeng.ltsystem.mainpreferences", "large_store");
        }
        return _remote;
    }

    static class Store extends PreferenceStore {

        static int[] LARGE_STORE_KEYS = {
                R.string.key_text_actions_set
        };

        public Store(String keyPrefix) {
            super(keyPrefix);
        }

        @Override
        public void putString(String key, String value) {
            if (isLargeKey(key)) {
                getSharedPreferences().edit().putString(key, value).commit();
            } else {
                super.putString(key, value);
            }
        }

        @Override
        public void putStringSet(String key, Set<String> values) {
            if (isLargeKey(key)) {
                getSharedPreferences().edit().putStringSet(key, values).commit();
            } else {
                super.putStringSet(key, values);
            }
        }

        private boolean isLargeKey(String key) {
            return Arrays.stream(LARGE_STORE_KEYS).filter(k -> getKeyById(k).equals(key)).count() > 0;
        }

        private SharedPreferences getSharedPreferences() {
            return ILTweaks.currentApplication().getSharedPreferences("large_store", 0);
        }
    }
}
