package li.lingfeng.ltsystem.prefs;

import android.os.SystemProperties;
import android.preference.PreferenceDataStore;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

public class PreferenceStore implements PreferenceDataStore {

    private String mKeyPrefix;

    public PreferenceStore(String keyPrefix) {
        mKeyPrefix = keyPrefix;
    }

    public void putString(int key, String value) {
        putString(getKeyById(key), value);
    }

    @Override
    public void putString(String key, String value) {
        SystemProperties.set(mKeyPrefix + key, value);
    }

    public void putStringSet(int key, Set<String> values) {
        putStringSet(getKeyById(key), values);
    }

    @Override
    public void putStringSet(String key, Set<String> values) {
        SystemProperties.set(mKeyPrefix + key, StringUtils.join(values, '\n'));
    }

    public void putInt(int key, int value) {
        putInt(getKeyById(key), value);
    }

    @Override
    public void putInt(String key, int value) {
        SystemProperties.set(mKeyPrefix + key, String.valueOf(value));
    }

    public void putLong(int key, long value) {
        putLong(getKeyById(key), value);
    }

    @Override
    public void putLong(String key, long value) {
        SystemProperties.set(mKeyPrefix + key, String.valueOf(value));
    }

    public void putFloat(int key, float value) {
        putFloat(getKeyById(key), value);
    }

    @Override
    public void putFloat(String key, float value) {
        SystemProperties.set(mKeyPrefix + key, String.valueOf(value));
    }

    public void putBoolean(int key, boolean value) {
        putBoolean(getKeyById(key), value);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        SystemProperties.set(mKeyPrefix + key, String.valueOf(value));
    }

    public String getString(int key, String defValue) {
        return getString(getKeyById(key), defValue);
    }

    @Override
    public String getString(String key, String defValue) {
        return SystemProperties.get(mKeyPrefix + key, defValue);
    }

    public Set<String> getStringSet(int key, Set<String> defValues) {
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

    public int getInt(int key, int defValue) {
        return getInt(getKeyById(key), defValue);
    }

    @Override
    public int getInt(String key, int defValue) {
        return SystemProperties.getInt(mKeyPrefix + key, defValue);
    }

    public long getLong(int key, long defValue) {
        return getLong(getKeyById(key), defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return SystemProperties.getLong(mKeyPrefix + key, defValue);
    }

    public float getFloat(int key, float defValue) {
        return getFloat(getKeyById(key), defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return Float.parseFloat(SystemProperties.get(mKeyPrefix + key, String.valueOf(defValue)));
    }

    public boolean getBoolean(int key, boolean defValue) {
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
