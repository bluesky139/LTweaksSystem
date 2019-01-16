package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.utils.ContextUtils;

@MethodsLoad(packages = PackageNames.STEAM, prefs = R.string.key_steam_database)
public class SteamDatabase extends SteamBase {

    @Override
    protected String newMenuName() {
        return "Steam Database";
    }

    @Override
    protected int newMenuPriority() {
        return 4;
    }

    @Override
    protected int newMenuShowAsAction() {
        return MenuItem.SHOW_AS_ACTION_NEVER;
    }

    @Override
    protected void menuItemSelected(Activity activity) throws Throwable {
        String url = getUrl(activity);
        if (url == null) {
            return;
        }

        Pattern pattern = Pattern.compile("^https?://store\\.steampowered\\.com/app/(\\d+)/");
        Matcher matcher = pattern.matcher(url);
        if (!matcher.find()) {
            Toast.makeText(activity, "Can't find game id.", Toast.LENGTH_SHORT).show();
            return;
        }

        String gameId = matcher.group(1);
        ContextUtils.startBrowser(activity, "https://steamdb.info/app/" + gameId + "/");
    }
}
