package li.lingfeng.ltsystem.tweaks.google;

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
}, prefs = R.string.key_chrome_archive_is)
public class ChromeArchiveIs extends ChromeBase {

    @Override
    protected Map<String, MenuInfo> newMenus() {
        Map<String, MenuInfo> infos = new HashMap<>(1);
        String title = ContextUtils.getLString(R.string.chrome_archive_is);
        infos.put(title, new MenuInfo(title, 1004, (activity, url, isCustomTab) -> {
            loadUrl(activity, "https://archive.is/newest/" + url);
        }));
        return infos;
    }
}
