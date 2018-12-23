package li.lingfeng.ltsystem.tweaks;

import android.app.Activity;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.ILTweaksMethods;

public class TweakBase extends ILTweaksMethods {

    protected ClassLoader getClassLoader() {
        return ILTweaks.currentApplication().getClassLoader();
    }

    protected Class findClass(String className) throws ClassNotFoundException {
        return Class.forName(className, false, getClassLoader());
    }

    protected void addHookOnActivity(String className, ILTweaks.MethodParam param, ILTweaks.MethodHook hook) {
        final Activity activity = (Activity) param.thisObject;
        if (activity.getClass().getName().equals(className)) {
            param.addHook(hook);
        }
    }
}
