package li.lingfeng.ltsystem.tweaks;

import android.app.Activity;
import android.app.Service;
import android.view.KeyEvent;
import android.view.Window;

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

    protected void beforeOnClass(String className, ILTweaks.MethodParam param, ILTweaks.Before before) {
        if (param.thisObject.getClass().getName().equals(className)) {
            param.before(before);
        }
    }

    protected void afterOnClass(String className, ILTweaks.MethodParam param, ILTweaks.After after) {
        if (param.thisObject.getClass().getName().equals(className)) {
            param.after(after);
        }
    }

    protected void beforeOnBackPressed(String className, ILTweaks.MethodParam param, ILTweaks.Before before) {
        if (param.thisObject.getClass().getName().equals(className)) {
            int keyCode = (int) param.args[0];
            KeyEvent event = (KeyEvent) param.args[1];
            if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking() && !event.isCanceled()) {
                param.before(before);
            }
        }
    }

    protected void afterOnBackPressed(String className, ILTweaks.MethodParam param, ILTweaks.After after) {
        if (param.thisObject.getClass().getName().equals(className)) {
            int keyCode = (int) param.args[0];
            KeyEvent event = (KeyEvent) param.args[1];
            if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking() && !event.isCanceled()) {
                param.after(after);
            }
        }
    }

    protected void beforeOnCreateOptionsMenu(String className, ILTweaks.MethodParam param, ILTweaks.Before before) {
        final Activity activity = (Activity) param.thisObject;
        if (activity.getClass().getName().equals(className)) {
            int featureId = (int) param.args[0];
            if (featureId == Window.FEATURE_OPTIONS_PANEL) {
                param.before(before);
            }
        }
    }

    protected void afterOnCreateOptionsMenu(String className, ILTweaks.MethodParam param, ILTweaks.After after) {
        final Activity activity = (Activity) param.thisObject;
        if (activity.getClass().getName().equals(className)) {
            int featureId = (int) param.args[0];
            if (featureId == Window.FEATURE_OPTIONS_PANEL) {
                param.after(after);
            }
        }
    }

    protected void beforeOnOptionsItemSelected(String className, ILTweaks.MethodParam param, ILTweaks.Before before) {
        final Activity activity = (Activity) param.thisObject;
        if (activity.getClass().getName().equals(className)) {
            int featureId = (int) param.args[0];
            if (featureId == Window.FEATURE_OPTIONS_PANEL) {
                param.before(before);
            }
        }
    }

    protected void afterOnOptionsItemSelected(String className, ILTweaks.MethodParam param, ILTweaks.After after) {
        final Activity activity = (Activity) param.thisObject;
        if (activity.getClass().getName().equals(className)) {
            int featureId = (int) param.args[0];
            if (featureId == Window.FEATURE_OPTIONS_PANEL) {
                param.after(after);
            }
        }
    }
}
