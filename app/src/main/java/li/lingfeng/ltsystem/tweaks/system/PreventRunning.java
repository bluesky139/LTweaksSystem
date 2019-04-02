package li.lingfeng.ltsystem.tweaks.system;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.alibaba.fastjson.JSONArray;

import java.util.HashSet;
import java.util.Set;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.LTHelper;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

public abstract class PreventRunning extends TweakBase {

    protected JSONArray mPreventList;
    protected Set<Integer> mPreventUids = new HashSet<>();

    public PreventRunning() {
        mPreventList = Prefs.large().getArray(getPreventListKey(), new JSONArray());
        mPreventList.forEach((line) -> {
            Logger.d(getClass().getSimpleName() + " list item: " + line);
        });
    }

    protected abstract int getPreventListKey();

    @Override
    public void com_android_server_am_ActivityManagerService__finishBooting__(ILTweaks.MethodParam param) {
        param.after(() -> {
            if (mPreventUids.size() > 0) {
                return;
            }
            Context context = LTHelper.currentApplication();
            mPreventList.forEach((line) -> {
                try {
                    ApplicationInfo info = context.getPackageManager().getApplicationInfo((String) line, PackageManager.GET_META_DATA);
                    mPreventUids.add(info.uid);
                    Logger.d(getClass().getSimpleName() + " list item uid: " + info.uid);
                } catch (Exception e)
                {}
            });
        });
    }
}
