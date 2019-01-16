package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.view.MenuItem;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.STEAM, prefs = R.string.key_steam_go_reviews)
public class SteamGoReviews extends SteamBase {

    @Override
    protected String newMenuName() {
        return "Go Reviews";
    }

    @Override
    protected int newMenuPriority() {
        return 2;
    }

    @Override
    protected int newMenuShowAsAction() {
        return MenuItem.SHOW_AS_ACTION_NEVER;
    }

    @Override
    protected void menuItemSelected(Activity activity) throws Throwable {
        Logger.i("Steam go reviews.");
        ViewUtils.executeJs(getWebView(activity), "document.getElementById('Reviews_summary').scrollIntoView();");
    }
}
