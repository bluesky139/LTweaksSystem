package li.lingfeng.ltsystem.tweaks.google;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;

public class ChromeOpenWith {

    @MethodsLoad(packages = {
            PackageNames.CHROME,
            PackageNames.CHROME_BETA,
            PackageNames.CHROME_DEV,
            PackageNames.CHROME_CANARY,
    }, prefs = R.string.key_chrome_open_with)
    public static class Chrome extends ChromeBase {
        private static final String EXTRA_AUTO_LAUNCH_SINGLE_CHOICE = "android.intent.extra.AUTO_LAUNCH_SINGLE_CHOICE";
        @Override
        protected Map<String, MenuInfo> newMenus() {
            Map<String, MenuInfo> infos = new HashMap<>(1);
            String title = ContextUtils.getLString(R.string.chrome_open_with);
            infos.put(title, new MenuInfo(title, 1006, (activity, url, isCustomTab) -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                intent.putExtra("ltweaks_activities_without_preferred_filter", true);
                intent.putExtra("ltweaks_remove_package", getPackageName());
                Intent chooserIntent = Intent.createChooser(intent, title);
                chooserIntent.putExtra(EXTRA_AUTO_LAUNCH_SINGLE_CHOICE, false);
                activity.startActivity(chooserIntent);
            }));
            return infos;
        }
    }

    @MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_chrome_open_with)
    public static class Android extends TweakBase {
        private static final String CHOOSER_ACTIVITY = "com.android.internal.app.ChooserActivity";

        @Override
        public void com_android_server_pm_PackageManagerService__filterCandidatesWithDomainPreferredActivitiesLPr__Intent_int_List$ResolveInfo$_CrossProfileDomainInfo_int(ILTweaks.MethodParam param) {
            param.before(() -> {
                Intent intent = (Intent) param.args[0];
                if (intent.getBooleanExtra("ltweaks_activities_without_preferred_filter", false)) {
                    Logger.d("Return whole resolve infos in filterCandidatesWithDomainPreferredActivitiesLPr().");
                    List<ResolveInfo> infos = (List<ResolveInfo>) param.args[2];
                    param.setResult(infos);
                }
            });
        }

        @Override
        public void com_android_internal_app_ResolverActivity__shouldAutoLaunchSingleChoice__TargetInfo(ILTweaks.MethodParam param) {
            param.before(() -> {
                Activity activity = (Activity) param.thisObject;
                if (activity.getClass().getName().equals(CHOOSER_ACTIVITY)) {
                    Logger.d("shouldAutoLaunchSingleChoice false");
                    param.setResult(false);
                }
            });
        }
    }
}
