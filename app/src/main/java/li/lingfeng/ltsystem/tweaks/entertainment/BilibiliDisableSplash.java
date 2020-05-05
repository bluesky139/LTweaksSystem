package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageParser;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

public class BilibiliDisableSplash extends TweakBase {

    private static final String SPLASH_ACTIVITY = "tv.danmaku.bili.ui.splash.SplashActivity";
    private static final String HOT_SPLASH_ACTIVITY = "tv.danmaku.bili.ui.splash.HotSplashActivity";
    private static final String MAIN_ACTIVITY = "tv.danmaku.bili.MainActivityV2";

    @MethodsLoad(packages = PackageNames.BILIBILI, prefs = R.string.key_bilibili_disable_splash)
    public static class Bilibili extends TweakBase {

        @Override
        public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
            beforeOnClass(SPLASH_ACTIVITY, param, () -> {
                Activity activity = (Activity) param.thisObject;
                if (!activity.isFinishing()) {
                    Logger.v("Skip SplashActivity.");
                    Intent intent = new Intent();
                    intent.setClassName(PackageNames.BILIBILI, MAIN_ACTIVITY);
                    activity.startActivity(intent);
                    activity.finish();
                }
            });
        }

        @Override
        public void android_app_Activity__startActivityForResult__Intent_int_Bundle(ILTweaks.MethodParam param) {
            param.before(() -> {
                Intent intent = (Intent) param.args[0];
                if (intent.getComponent() != null && HOT_SPLASH_ACTIVITY.equals(intent.getComponent().getClassName())) {
                    Logger.v("Disallow HotSplashActivity.");
                    param.setResult(null);
                }
            });
        }
    }

    @MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_bilibili_disable_splash)
    public static class Android extends TweakBase {

        @Override
        public void android_content_pm_PackageParser__parsePackage__File_int_boolean(ILTweaks.MethodParam param) {
            param.after(() -> {
                PackageParser.Package pkg = (PackageParser.Package) param.getResult();
                if (pkg == null || pkg.packageName != PackageNames.BILIBILI) {
                    return;
                }
                PackageParser.Activity splashActivity = pkg.activities.stream().filter(activity -> activity.info.name.equals(SPLASH_ACTIVITY)).findFirst().get();
                if (splashActivity.intents.size() != 1) {
                    Logger.e("Bilibili SplashActivity has multi intents.");
                    return;
                }
                PackageParser.Activity mainActivity = pkg.activities.stream().filter(activity -> activity.info.name.equals(MAIN_ACTIVITY)).findFirst().get();
                if (mainActivity.intents.size() != 0) {
                    Logger.e("Bilibili MainActivity has intents.");
                    return;
                }
                Logger.i("Set " + MAIN_ACTIVITY + " as launcher and exported.");
                PackageParser.ActivityIntentInfo intent = splashActivity.intents.get(0);
                intent.activity = mainActivity;
                mainActivity.intents.add(intent);
                mainActivity.info.exported = true;
                mainActivity.info.launchMode = ActivityInfo.LAUNCH_SINGLE_TOP;
                splashActivity.intents.remove(0);
            });
        }
    }
}
