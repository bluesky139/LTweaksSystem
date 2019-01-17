package li.lingfeng.ltsystem.tweaks.system;

import android.app.Activity;
import android.app.Fragment;
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

    protected static final String INSTALLED_APP_DETAILS = "com.android.settings.applications.InstalledAppDetails";

    @Override
    public void android_app_Fragment__performCreateOptionsMenu__Menu_MenuInflater(ILTweaks.MethodParam param) {
        afterOnClass(INSTALLED_APP_DETAILS, param, () -> {
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
    public void android_app_Fragment__performOptionsItemSelected__MenuItem(ILTweaks.MethodParam param) {
        beforeOnClass(INSTALLED_APP_DETAILS, param, () -> {
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

    protected Activity getActivity(ILTweaks.MethodParam param) {
        return ((Fragment) param.thisObject).getActivity();
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
