package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.DOUBAN, prefs = R.string.key_douban_move_task_to_back)
public class DoubanMoveTaskToBack extends TweakBase {

    private static final String FACADE_ACTIVITY = "com.douban.frodo.activity.FacadeActivity";
    private int mCount = 0;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        param.after(() -> {
            if (param.thisObject.getClass().getName().equals(FACADE_ACTIVITY)) {
                mCount = 1;
                Logger.d("MoveTaskToBack onCreate " + mCount + ", FacadeActivity.");
            } else if (mCount == 1) {
                ++mCount;
                Logger.d("MoveTaskToBack onCreate " + mCount + ", " + param.thisObject.getClass().getName());
            }
        });
    }

    @Override
    public void android_app_Activity__onKeyUp__int_KeyEvent(ILTweaks.MethodParam param) {
        afterOnBackPressed(param, () -> {
            if (mCount == 1) {
                Activity activity = (Activity) param.thisObject;
                if (activity.isFinishing()) {
                    Logger.d("MoveTaskToBack finishing last activity, " + param.thisObject.getClass().getName());
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
