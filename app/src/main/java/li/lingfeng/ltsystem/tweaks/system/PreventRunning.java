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

    protected static JSONArray sPreventList;
    protected static Set<Integer> sPreventUids = new HashSet<>();

    public PreventRunning() {
        if (sPreventList == null) {
            sPreventList = Prefs.large().getArray(getPreventListKey(), new JSONArray());
            sPreventList.forEach((line) -> {
                Logger.d("Prevent list item: " + line);
            });
        }
    }

    protected abstract int getPreventListKey();

    @Override
    public void com_android_server_am_ActivityManagerService__finishBooting__(ILTweaks.MethodParam param) {
        param.after(() -> {
            if (sPreventUids.size() > 0) {
                return;
            }
            Context context = LTHelper.currentApplication();
            sPreventList.forEach((line) -> {
                try {
                    ApplicationInfo info = context.getPackageManager().getApplicationInfo((String) line, PackageManager.GET_META_DATA);
                    sPreventUids.add(info.uid);
                    Logger.d("Prevent list item uid: " + info.uid);
                } catch (Exception e)
                {}
            });
        });
    }
}
