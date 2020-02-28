package li.lingfeng.ltsystem.tweaks.system;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;
import li.lingfeng.ltsystem.utils.Utils;

public abstract class AppInfo extends TweakBase {

    @Override
    public void com_android_settings_applications_appinfo_AppInfoDashboardFragment__onCreateOptionsMenu__Menu_MenuInflater(ILTweaks.MethodParam param) {
        param.after(() -> {
            final Pair[] names = newMenuNames(param);
            if (names == null || names.length == 0) {
                return;
            }

            for (Pair<String, Integer> pair : names) {
                String name = pair.first;
                int priority = pair.second;
                Logger.i("New menu " + name);
                Menu menu = (Menu) param.args[0];
                menu.add(Menu.NONE, Menu.NONE, priority, name);
            }
        });
    }

    @Override
    public void com_android_settings_applications_appinfo_AppInfoDashboardFragment__onOptionsItemSelected__MenuItem(ILTweaks.MethodParam param) {
        param.before(() -> {
            final Pair[] names = newMenuNames(param);
            if (names == null || names.length == 0) {
                return;
            }

            MenuItem item = (MenuItem) param.args[0];
            if (!Utils.pairContains(names, item.getTitle(), true)) {
                return;
            }
            Logger.i("Menu " + item.getTitle() + " click.");
            menuItemSelected(item.getTitle(), param);
            param.setResult(true);
        });
    }

    protected Activity getActivity(ILTweaks.MethodParam param) throws Throwable {
        return (Activity) ReflectUtils.callMethod(param.thisObject, "getActivity");
    }

    protected String getPackageName(ILTweaks.MethodParam param) throws Throwable {
        return (String) ReflectUtils.getObjectField(param.thisObject, "mPackageName");
    }

    protected ApplicationInfo getApplicationInfo(ILTweaks.MethodParam param) throws Throwable {
        Object appEntry = ReflectUtils.getObjectField(param.thisObject, "mAppEntry");
        return (ApplicationInfo) ReflectUtils.getObjectField(appEntry, "info");
    }

    protected abstract Pair<String, Integer>[] newMenuNames(ILTweaks.MethodParam param) throws Throwable;
    protected abstract void menuItemSelected(CharSequence menuName, ILTweaks.MethodParam param) throws Throwable;
}
