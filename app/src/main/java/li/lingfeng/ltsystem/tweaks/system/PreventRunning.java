package li.lingfeng.ltsystem.tweaks.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

import li.lingfeng.ltsystem.ILTPrefListener;
import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.LTHelper;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

public abstract class PreventRunning extends TweakBase {

    private List<String> mPreventList;
    private int[] mPreventUids;

    protected abstract int getPreventListKey();

    protected List<String> getPreventList() {
        if (mPreventList == null) {
            mPreventList = Prefs.large().getStringList(getPreventListKey(), new ArrayList<>(), false);
            mPreventList.forEach((line) -> {
                Logger.d(getClass().getSimpleName() + " list item: " + line);
            });
        }
        return mPreventList;
    }

    protected int[] getPreventUids() {
        return mPreventUids != null ? mPreventUids : new int[0];
    }

    private void refreshUids() {
        List<String> list = getPreventList();
        mPreventUids = new int[list.size()];
        for (int i = 0; i < mPreventUids.length; ++i) {
            String packageName = list.get(i);
            try {
                ApplicationInfo info = LTHelper.currentApplication().getPackageManager().getApplicationInfo(packageName, 0);
                mPreventUids[i] = info.uid;
                Logger.d(getClass().getSimpleName() + " list item uid: " + info.uid);
            } catch (Throwable e) {
                mPreventUids[i] = -1;
                Logger.w(getClass().getSimpleName() + " list item unknown uid - " + packageName);
            }
        }
    }

    @Override
    public void com_android_server_am_ActivityManagerService__finishBooting__(ILTweaks.MethodParam param) {
        param.after(() -> {
            if (mPreventUids != null) {
                return;
            }
            refreshUids();
            Prefs.large().addListener(getPreventListKey(), new ILTPrefListener.Stub() {
                @Override
                public void onPrefChanged(String key) throws RemoteException {
                    mPreventList = null;
                    getPreventList();
                    refreshUids();
                }
            });

            Logger.d(getClass().getSimpleName() + " register package added/removed broadcast.");
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_PACKAGE_ADDED);
            filter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
            filter.addDataScheme("package");
            LTHelper.currentApplication().registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String packageName = intent.getData().getSchemeSpecificPart();
                    Logger.v(PreventRunning.this.getClass().getSimpleName() + " receive " + intent.getAction() + " on " + packageName);
                    if (getPreventList().contains(packageName)) {
                        refreshUids();
                    }
                }
            }, filter);
        });
    }
}
