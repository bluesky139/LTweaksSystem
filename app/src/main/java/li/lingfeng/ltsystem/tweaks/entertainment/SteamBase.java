package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;

public abstract class SteamBase extends TweakBase {

    protected static final String MAIN_ACTIVITY = "com.valvesoftware.android.steam.community.activity.MainActivity";

    @Override
    public void android_app_Activity__onPreparePanel__int_View_Menu(ILTweaks.MethodParam param) {
        afterOnPrepareOptionsMenu(MAIN_ACTIVITY, param, () -> {
            Menu menu = (Menu) param.args[2];
            String title = newMenuName();
            Logger.i("Add menu " + title);
            MenuItem item = menu.add(Menu.NONE, Menu.NONE, newMenuPriority(), title);
            item.setShowAsAction(newMenuShowAsAction());
        });
    }

    @Override
    public void android_app_Activity__onMenuItemSelected__int_MenuItem(ILTweaks.MethodParam param) {
        beforeOnOptionsItemSelected(MAIN_ACTIVITY, param, () -> {
            MenuItem item = (MenuItem) param.args[1];
            if (!newMenuName().equals(item.getTitle())) {
                return;
            }
            Logger.i("Menu item " + item.getTitle() + " selected.");

            Activity activity = (Activity) param.thisObject;
            try {
                menuItemSelected(activity);
            } catch (Throwable e) {
                Logger.stackTrace(e);
            }
            param.setResult(true);
        });
    }

    protected WebView getWebView(Activity activity) {
        int idWebView = ContextUtils.getResId("webView", "id");
        return (WebView) activity.findViewById(idWebView);
    }

    protected String getUrl(Activity activity) {
        WebView webView = getWebView(activity);
        if (webView == null) {
            Toast.makeText(activity, "Error.", Toast.LENGTH_SHORT).show();
            return null;
        }

        String url = webView.getUrl();
        Logger.i("Got url " + url);
        return url;
    }

    protected abstract String newMenuName();
    protected abstract int newMenuPriority();
    protected abstract int newMenuShowAsAction();
    protected abstract void menuItemSelected(Activity activity) throws Throwable;
}
