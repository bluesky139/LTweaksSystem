package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.view.MenuItem;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.utils.ShareUtils;

@MethodsLoad(packages = PackageNames.STEAM, prefs = R.string.key_steam_share_url)
public class SteamShare extends SteamBase {
    @Override
    protected String newMenuName() {
        return "Share";
    }

    @Override
    protected int newMenuPriority() {
        return 3;
    }

    @Override
    protected int newMenuShowAsAction() {
        return MenuItem.SHOW_AS_ACTION_NEVER;
    }

    @Override
    protected void menuItemSelected(Activity activity) throws Throwable {
        String url = getUrl(activity);
        if (url != null) {
            ShareUtils.shareText(activity, url);
        }
    }
}
