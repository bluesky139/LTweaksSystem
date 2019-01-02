package li.lingfeng.ltsystem.prefs;

import android.content.Context;

import com.crossbowffs.remotepreferences.RemotePreferences;

import java.util.Set;

public class RemotePreference extends RemotePreferences {

    public RemotePreference(Context context, String authority, String prefName) {
        super(context, authority, prefName);
    }

    public String getString(int key, String defValue) {
        return super.getString(getKeyById(key), defValue);
    }

    public Set<String> getStringSet(int key, Set<String> defValues) {
        return super.getStringSet(getKeyById(key), defValues);
    }

    public int getInt(int key, int defValue) {
        return super.getInt(getKeyById(key), defValue);
    }

    public long getLong(int key, long defValue) {
        return super.getLong(getKeyById(key), defValue);
    }

    public float getFloat(int key, float defValue) {
        return super.getFloat(getKeyById(key), defValue);
    }

    public boolean getBoolean(int key, boolean defValue) {
        return super.getBoolean(getKeyById(key), defValue);
    }

    private String getKeyById(int id) {
        return PrefKeys.getById(id);
    }
}
