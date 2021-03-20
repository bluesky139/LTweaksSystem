package li.lingfeng.ltsystem.tweaks;

import android.app.Activity;
import android.net.Uri;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.utils.Logger;

public abstract class TweakMoveTaskToBack extends TweakBase {

    protected int mCount = 0;
    protected abstract String getLaunchActivity();

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        param.after(() -> {
            Activity activity = (Activity) param.thisObject;
            if (activity.getClass().getName().equals(getLaunchActivity())) {
                return;
            }
            Uri referrer = activity.getReferrer();
            if (referrer != null && !referrer.toString().equals("android-app://" + getPackageName())) {
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
