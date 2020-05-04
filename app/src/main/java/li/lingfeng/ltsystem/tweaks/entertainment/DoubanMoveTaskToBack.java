package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.net.Uri;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.DOUBAN, prefs = R.string.key_douban_move_task_to_back)
public class DoubanMoveTaskToBack extends TweakBase {

    private static final String SPLASH_ACTIVITY = "com.douban.frodo.activity.SplashActivity";
    private static final String REFERRER_DOUBAN = "android-app://" + PackageNames.DOUBAN;
    private int mCount = 0;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        param.after(() -> {
            Activity activity = (Activity) param.thisObject;
            if (activity.getClass().getName().equals(SPLASH_ACTIVITY)) {
                return;
            }
            Uri referrer = activity.getReferrer();
            if (referrer != null && !referrer.toString().equals(REFERRER_DOUBAN)) {
                mCount = 1;
                Logger.d("MoveTaskToBack onCreate " + mCount + ", " + activity.getClass().getName());
            } else if (mCount >= 1) {
                ++mCount;
                Logger.d("MoveTaskToBack onCreate " + mCount + ", " + activity.getClass().getName());
            }
        });
    }

    @Override
    public void android_app_Activity__onKeyUp__int_KeyEvent(ILTweaks.MethodParam param) {
        afterOnBackPressed(param, () -> {
            if (mCount == 1) {
                Activity activity = (Activity) param.thisObject;
                if (activity.isFinishing()) {
                    Logger.d("MoveTaskToBack finishing last activity, " + activity.getClass().getName());
                    mCount = 0;
                    activity.moveTaskToBack(true);
                }
            }
        });
    }

    @Override
    public void android_app_Activity__onDestroy__(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (mCount > 0) {
                --mCount;
                Logger.d("MoveTaskToBack onDestroy " + mCount + ", " + param.thisObject.getClass().getName());
            }
        });
    }
}
