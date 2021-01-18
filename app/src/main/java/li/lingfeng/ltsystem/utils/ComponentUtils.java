package li.lingfeng.ltsystem.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageManager;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.LTHelper;

/**
 * Created by smallville on 2017/2/1.
 */

public class ComponentUtils {

    public static void enableComponent(String componentCls, boolean enabled) {
        ComponentName componentName = new ComponentName(LTHelper.currentApplication(), componentCls);
        LTHelper.currentApplication().getPackageManager().setComponentEnabledSetting(componentName,
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static void enableComponent(Class<?> componentCls, boolean enabled) {
        enableComponent(componentCls.getName(), enabled);
    }

    public static boolean isComponentEnabled(String componentCls) {
        ComponentName componentName = new ComponentName(LTHelper.currentApplication(), componentCls);
        return LTHelper.currentApplication().getPackageManager().getComponentEnabledSetting(componentName)
                == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
    }

    public static boolean isComponentEnabled(Class<?> componentCls) {
        return isComponentEnabled(componentCls.getName());
    }

    public static boolean isAlias(Activity activity) {
        String name = activity.getIntent().getComponent().getClassName();
        return !name.equals(activity.getClass().getName());
    }

    public static String getAlias(Activity activity) {
        if (!isAlias(activity)) {
            return null;
        }
        String name = activity.getIntent().getComponent().getClassName();
        return name.substring(activity.getClass().getPackage().getName().length() + 1,
                name.length() - activity.getClass().getSimpleName().length());
    }

    public static String getFullAliasName(Class originalCls, String alias) {
        String[] s = Utils.splitByLastChar(originalCls.getName(), '.');
        return s[0] + "." + alias + s[1];
    }
}
