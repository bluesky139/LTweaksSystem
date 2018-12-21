package li.lingfeng.ltsystem.prefs;

import android.os.SystemProperties;
import android.preference.PreferenceDataStore;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import org.apache.commons.collections4.CollectionUtils;

import java.util.HashSet;
import java.util.Set;

public class PreferenceStore implements PreferenceDataStore {

    private String mKeyPrefix;

    public PreferenceStore(String keyPrefix) {
        mKeyPrefix = keyPrefix;
    }

    @Override
    public void putString(String key, String value) {

    }

    @Override
    public void putStringSet(String key, Set<String> values) {

    }

    @Override
    public void putInt(String key, int value) {

    }

    @Override
    public void putLong(String key, long value) {

    }

    @Override
    public void putFloat(String key, float value) {

    }

    @Override
    public void putBoolean(String key, boolean value) {

    }

    public String getString(@StringRes int key, String defValue) {
        return getString(getKeyById(key), defValue);
    }

    @Override
    public String getString(String key, String defValue) {
        return SystemProperties.get(mKeyPrefix + key, defValue);
    }

    public Set<String> getStringSet(@StringRes int key, Set<String> defValues) {
        return getStringSet(getKeyById(key), defValues);
    }

    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        String value = SystemProperties.get(mKeyPrefix + key);
        if (value.isEmpty()) {
            return defValues;
        }
        String[] values = value.split("\n");
        Set<String> set = new HashSet<>(values.length);
        CollectionUtils.addAll(set, values);
        return set;
    }

    public int getInt(@StringRes int key, int defValue) {
        return getInt(getKeyById(key), defValue);
    }

    @Override
    public int getInt(String key, int defValue) {
        return SystemProperties.getInt(mKeyPrefix + key, defValue);
    }

    public long getLong(@StringRes int key, long defValue) {
        return getLong(getKeyById(key), defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return SystemProperties.getLong(mKeyPrefix + key, defValue);
    }

    public float getFloat(@StringRes int key, float defValue) {
        return getFloat(getKeyById(key), defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return Float.parseFloat(SystemProperties.get(mKeyPrefix + key, String.valueOf(defValue)));
    }

    public boolean getBoolean(@StringRes int key, boolean defValue) {
        return getBoolean(getKeyById(key), defValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return SystemProperties.getBoolean(mKeyPrefix + key, defValue);
    }

    private String getKeyById(int id) {
        return PrefKeys.getById(id);
    }
}
