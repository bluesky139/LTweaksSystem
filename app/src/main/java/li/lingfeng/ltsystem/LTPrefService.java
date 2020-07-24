package li.lingfeng.ltsystem;

import android.content.Context;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.utils.Logger;

public class LTPrefService extends ILTPref.Stub {

    private JSONObject mData;
    private Map<String, RemoteCallbackList<ILTPrefListener>> mListeners = new HashMap<>();

    public LTPrefService(Context context) {
        mData = loadFromDisk();
    }

    public static void register(Context context) {
        Logger.v("Register LTPrefService.");
        LTPrefService service = new LTPrefService(context);
        ServiceManager.addService("ltweaks_pref", service, true);
        Logger.i("LTPrefService is registered.");
    }

    private JSONObject loadFromDisk() {
        try {
            String content = "{}";
            File file = new File(Prefs.LARGE_STORE_PATH);
            if (file.exists()) {
                content = FileUtils.readFileToString(file, "UTF-8");
            }
            return JSON.parseObject(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveToDisk(JSONObject data) {
        try {
            File file = new File(Prefs.LARGE_STORE_PATH);
            FileUtils.writeStringToFile(file, data.toString(), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getStringList(String key) throws RemoteException {
        JSONArray jArray = mData.getJSONArray(key);
        return jArray != null ? jArray.toJavaList(String.class) : null;
    }

    @Override
    public void putStringList(String key, List<String> value) throws RemoteException {
        JSONArray jArray = new JSONArray((List<Object>) (List) value);
        mData.put(key, jArray);
        saveToDisk(mData);
        notifyPrefChanged(key);
    }

    @Override
    public void appendStringToList(String key, String value, int limit) throws RemoteException {
        JSONArray jArray = mData.getJSONArray(key);
        if (jArray == null) {
            jArray = new JSONArray();
            mData.put(key, jArray);
        }
        jArray.add(value);
        if (jArray.size() > limit) {
            jArray.remove(0);
        }
        saveToDisk(mData);
        notifyPrefChanged(key);
    }

    @Override
    public void addListener(String key, ILTPrefListener listener) throws RemoteException {
        RemoteCallbackList<ILTPrefListener> listeners = mListeners.get(key);
        if (listeners == null) {
            listeners = new RemoteCallbackList<>();
            mListeners.put(key, listeners);
        }
        listeners.register(listener);
    }

    @Override
    public void removeListener(String key, ILTPrefListener listener) throws RemoteException {
        RemoteCallbackList<ILTPrefListener> listeners = mListeners.get(key);
        if (listeners != null) {
            listeners.unregister(listener);
        }
    }

    private void notifyPrefChanged(String key) {
        RemoteCallbackList<ILTPrefListener> listeners = mListeners.get(key);
        if (listeners != null) {
            int size = listeners.beginBroadcast();
            for (int i = 0; i < size; ++i) {
                ILTPrefListener listener = listeners.getBroadcastItem(i);
                try {
                    listener.onPrefChanged(key);
                } catch (RemoteException e) {}
            }
            listeners.finishBroadcast();
        }
    }
}
