package li.lingfeng.ltsystem.tweaks.system;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.util.Pair;

import java.util.HashMap;
import java.util.Map;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.utils.ContextUtils;

@MethodsLoad(packages = PackageNames.ANDROID_SETTINGS, prefs = R.string.key_app_info_go_market)
public class AppInfoGoMarket extends AppInfo {
    @Override
    protected Pair<String, Integer>[] newMenuNames() {
        return new Pair[] {
                Pair.create("Google Play", 1010),
                Pair.create("CoolApk", 1011),
                Pair.create("ApkPure", 1012),
                Pair.create("Mobilism", 1013),
                Pair.create("ApkMirror", 1014)
        };
    }

    @Override
    protected void menuItemSelected(CharSequence menuName, ILTweaks.MethodParam param) throws Throwable {
        Activity activity = getActivity(param);
        if ("Mobilism".equals(menuName)) {
            ApplicationInfo appInfo = getApplicationInfo(param);
            ContextUtils.searchInMobilism(activity, appInfo.loadLabel(activity.getPackageManager()));
        } else if ("ApkMirror".equals(menuName)) {
            String packageName = getPackageName(param);
            ContextUtils.searchInApkMirror(activity, packageName);
        } else {
            Map<String, String> marketNameToPackage = new HashMap<String, String>() {{
                put("Google Play", PackageNames.GOOGLE_PLAY);
                put("CoolApk", PackageNames.COOLAPK);
                put("ApkPure", PackageNames.APKPURE);
            }};
            String market = marketNameToPackage.get(menuName);
            if (market != null) {
                ContextUtils.openAppInMarket(activity, getPackageName(param), market);
            } else {
                throw new Exception("Unknown menu " + menuName);
            }
        }
    }
}
