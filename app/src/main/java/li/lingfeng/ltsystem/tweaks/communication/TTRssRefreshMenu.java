package li.lingfeng.ltsystem.tweaks.communication;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.TT_RSS, prefs = R.string.key_ttrss_refresh_menu)
public class TTRssRefreshMenu extends TweakBase {

    private static final String MASTER_ACTIVITY = "org.fox.ttrss.MasterActivity";
    private static final int ITEM_ID = 10001;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(MASTER_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            ViewGroup rootView = (ViewGroup) activity.findViewById(android.R.id.content);
            rootView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                try {
                    View fab = ViewUtils.findViewByName(activity, "master_fab");
                    if (fab != null) {
                        Logger.v("Remove master_fab.");
                        ViewUtils.removeView(fab);
                    }
                } catch (Throwable e) {
                    Logger.e("Hide headlines_fab exception.", e);
                }
            });
        });
    }

    @Override
    public void android_app_Activity__onCreatePanelMenu__int_Menu(ILTweaks.MethodParam param) {
        afterOnCreateOptionsMenu(MASTER_ACTIVITY, param, () -> {
            Menu menu = (Menu) param.args[1];
            if (menu != null && menu.findItem(ITEM_ID) == null) {
                Logger.i("Create refresh menu.");
                int idMenuGroup = ContextUtils.getIdId("menu_group_headlines");
                MenuItem menuItem = menu.add(idMenuGroup, ITEM_ID, Menu.NONE, ContextUtils.getLString(R.string.ttrss_refresh));
                menuItem.setIcon(ContextUtils.getDrawable("ic_refresh"));
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

                menu.findItem(ContextUtils.getIdId("headlines_select")).setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);
                menu.findItem(ContextUtils.getIdId("headlines_toggle_sort_order")).setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            }
        });
    }

    @Override
    public void android_app_Activity__onMenuItemSelected__int_MenuItem(ILTweaks.MethodParam param) {
        beforeOnOptionsItemSelected(MASTER_ACTIVITY, param, () -> {
            MenuItem menuItem = (MenuItem) param.args[1];
            if (menuItem.getItemId() == ITEM_ID) {
                Logger.i("Refresh headlines.");
                Object fragmentManager = ReflectUtils.callMethod(param.thisObject, "getSupportFragmentManager");
                Object headlinesFragment = ReflectUtils.callMethod(fragmentManager, "findFragmentByTag", "headlines");
                ReflectUtils.callMethod(headlinesFragment, "refresh",
                        new Object[] { false }, new Class[] { boolean.class });
                param.setResult(true);
            }
        });
    }
}
