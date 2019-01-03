package li.lingfeng.ltsystem.tweaks.system;

import android.content.Intent;
import android.content.pm.ResolveInfo;

import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.Set;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.IntentActions;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.ANDROID, prefs = {})
public class ShareFilter extends TweakBase {

    @Override
    public void com_android_server_pm_PackageManagerService__queryIntentActivitiesInternal__Intent_String_int_int_int_boolean_boolean(ILTweaks.MethodParam param) {
        param.addHook(new ILTweaks.MethodHook() {
            @Override
            public void after() throws Throwable {
                Intent intent = (Intent) param.args[0];
                if (!ArrayUtils.contains(IntentActions.sSendActions, intent.getAction())
                        || intent.getBooleanExtra("from_ltweaks", false)) {
                    return;
                }

                Set<String> activities = Prefs.remote().getStringSet(R.string.key_system_share_filter_activities, null);
                if (activities == null || activities.isEmpty()) {
                    return;
                }

                List<ResolveInfo> results = (List<ResolveInfo>) param.getResult();
                int removedCount = 0;
                for (int i = results.size() - 1; i >= 0; --i) {
                    ResolveInfo info = results.get(i);
                    if (activities.contains(info.activityInfo.applicationInfo.packageName + "/" + info.activityInfo.name)) {
                        results.remove(i);
                        ++removedCount;
                    }
                }
                Logger.i("Removed " + removedCount + " share activities for " + intent.getAction());
            }
        });
    }
}
