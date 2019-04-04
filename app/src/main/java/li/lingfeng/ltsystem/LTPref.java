package li.lingfeng.ltsystem;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;

import java.util.List;

import li.lingfeng.ltsystem.utils.Logger;

public class LTPref {

    private ILTPref mService;
    private static LTPref sInstance;

    public LTPref(Context context, ILTPref service) {
        mService = service;
    }

    public static LTPref instance() {
        if (sInstance == null) {
            IBinder binder = ServiceManager.getService("ltweaks_pref");
            if (binder == null) {
                throw new RuntimeException("Can't get ltweaks_pref service.");
            }
            ILTPref ltPref = ILTPref.Stub.asInterface(binder);
            sInstance = new LTPref(LTHelper.currentApplication(), ltPref);
        }
        return sInstance;
    }

    public List<String> getStringList(String key) {
        try {
            return mService.getStringList(key);
        } catch (RemoteException e) {
            Logger.stackTrace(e);
        }
        return null;
    }

    public void putStringList(String key, List<String> value) {
        try {
            mService.putStringList(key, value);
        } catch (RemoteException e) {
            Logger.stackTrace(e);
        }
    }

    public void addListener(String key, ILTPrefListener listener) {
        try {
            mService.addListener(key, listener);
        } catch (RemoteException e) {
            Logger.stackTrace(e);
        }
    }

    public void removeListener(String key, ILTPrefListener listener) {
        try {
            mService.removeListener(key, listener);
        } catch (RemoteException e) {
            Logger.stackTrace(e);
        }
    }
}
