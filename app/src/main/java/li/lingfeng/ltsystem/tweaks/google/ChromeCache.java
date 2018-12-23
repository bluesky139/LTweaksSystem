package li.lingfeng.ltsystem.tweaks.google;

import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.utils.ContextUtils;

@MethodsLoad(packages = {
        PackageNames.CHROME,
        PackageNames.CHROME_BETA,
        PackageNames.CHROME_DEV,
        PackageNames.CHROME_CANARY
}, prefs = R.string.key_chrome_google_cache)
public class ChromeCache extends ChromeBase {

    @Override
    protected Map<String, MenuInfo> newMenus() {
        Map<String, MenuInfo> infos = new HashMap<>(1);
        infos.put(ContextUtils.getLString(R.string.chrome_google_cache), new MenuInfo(1002, (activity, url, isCustomTab) -> {
            String cachedUrl = (url.startsWith("https") ? "https" : "http") + "://webcache.googleusercontent.com/search?q=cache:"
                    + Uri.encode(url);
            loadUrl(activity, cachedUrl);
        }));
        return infos;
    }
}
