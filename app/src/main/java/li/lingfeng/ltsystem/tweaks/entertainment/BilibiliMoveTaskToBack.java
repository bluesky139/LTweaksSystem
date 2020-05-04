package li.lingfeng.ltsystem.tweaks.entertainment;

import android.content.Intent;

import java.util.Arrays;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.tweaks.TweakMoveTaskToBack;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.BILIBILI, prefs = R.string.key_bilibili_move_task_to_back)
public class BilibiliMoveTaskToBack extends TweakMoveTaskToBack {

    private static final String MAIN_ACTIVITY = "tv.danmaku.bili.MainActivityV2";
    private static final String SPLASH_ACTIVITY = "tv.danmaku.bili.ui.splash.SplashActivity";

    @Override
    public void android_app_Activity__startActivities__Intent$array_Bundle(ILTweaks.MethodParam param) {
        param.before(() -> {
            Intent[] intents = (Intent[]) param.args[0];
            Intent[] newIntents = Arrays.stream(intents)
                    .filter(intent -> intent.getComponent() == null || !MAIN_ACTIVITY.equals(intent.getComponent().getClassName()))
                    .toArray(Intent[]::new);
            if (intents.length != newIntents.length) {
                Logger.d("Removed MainActivityV2 from startActivities, count " + intents.length + " -> " + newIntents.length);
            }
            param.setArg(0, newIntents);
        });
    }

    @Override
    protected String getLaunchActivity() {
        return Prefs.instance().getBoolean(R.string.key_bilibili_disable_splash, false) ? MAIN_ACTIVITY : SPLASH_ACTIVITY;
    }
}
