package li.lingfeng.ltsystem.tweaks.entertainment;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakMoveTaskToBack;

@MethodsLoad(packages = PackageNames.DOUBAN, prefs = R.string.key_douban_move_task_to_back)
public class DoubanMoveTaskToBack extends TweakMoveTaskToBack {

    private static final String SPLASH_ACTIVITY = "com.douban.frodo.activity.SplashActivity";

    @Override
    protected String getLaunchActivity() {
        return SPLASH_ACTIVITY;
    }
}
