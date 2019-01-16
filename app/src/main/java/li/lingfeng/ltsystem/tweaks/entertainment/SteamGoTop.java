package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.view.MenuItem;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.STEAM, prefs = R.string.key_steam_go_top)
public class SteamGoTop extends SteamBase {

    @Override
    protected String newMenuName() {
        return "Go Top";
    }

    @Override
    protected int newMenuPriority() {
        return 1;
    }

    @Override
    protected int newMenuShowAsAction() {
        return MenuItem.SHOW_AS_ACTION_NEVER;
    }

    @Override
    protected void menuItemSelected(Activity activity) throws Throwable {
        Logger.i("Steam go top.");
        ViewUtils.executeJs(getWebView(activity),
                "if (document.getElementsByClassName('page_title_area game_title_area page_content').length > 0) {\n"
                        + "  document.getElementsByClassName('page_title_area game_title_area page_content')[0].scrollIntoView();\n"
                        + "} else {\n"
                        + "  window.scrollTo(0, 0);\n"
                        + "}");
    }
}
