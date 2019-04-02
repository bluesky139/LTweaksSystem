package li.lingfeng.ltsystem.tweaks.system;

import android.content.Intent;
import android.view.View;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.PackageUtils;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.ANDROID_SYSTEM_UI, prefs = R.string.key_recent_task_swipe_to_kill)
public class RecentTaskSwipeToKill extends TweakBase {

    private static final String TASK_VIEW = "com.android.systemui.recents.views.TaskView";

    @Override
    public void com_android_systemui_SwipeHelper__dismissChild__View_float_boolean(ILTweaks.MethodParam param) {
        param.after(() -> {
            float transaction = (float) ReflectUtils.callMethod(param.thisObject, "getTranslation",
                    new Object[] { param.args[0] }, new Class[] { View.class });
            if (transaction < 0) {
                return;
            }

            View taskView = (View) param.args[0];
            if (!taskView.getClass().getName().equals(TASK_VIEW)) {
                return;
            }

            Logger.i("Swipe right on recent task to kill.");
            Object task = ReflectUtils.callMethod(taskView, "getTask");
            Object taskKey = ReflectUtils.getObjectField(task, "key");
            Intent intent = (Intent) ReflectUtils.getObjectField(taskKey, "baseIntent");
            String packageName = intent.getComponent().getPackageName();
            PackageUtils.killPackage(taskView.getContext(), packageName);
        });
    }
}
