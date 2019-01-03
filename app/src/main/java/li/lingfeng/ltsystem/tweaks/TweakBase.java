package li.lingfeng.ltsystem.tweaks;

import android.app.Activity;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.LTHelper;
import li.lingfeng.ltsystem.ILTweaksMethods;

public class TweakBase extends ILTweaksMethods {

    protected String getPackageName() {
        return LTHelper.currentApplication().getPackageName();
    }

    protected ClassLoader getClassLoader() {
        return LTHelper.currentApplication().getClassLoader();
    }

    protected Class findClass(String className) throws ClassNotFoundException {
        return Class.forName(className, false, getClassLoader());
    }

    protected void beforeOnActivity(String className, ILTweaks.MethodParam param, ILTweaks.Before before) {
        final Activity activity = (Activity) param.thisObject;
        if (activity.getClass().getName().equals(className)) {
            param.before(before);
        }
    }

    protected void afterOnActivity(String className, ILTweaks.MethodParam param, ILTweaks.After after) {
        final Activity activity = (Activity) param.thisObject;
        if (activity.getClass().getName().equals(className)) {
            param.after(after);
        }
    }
}
