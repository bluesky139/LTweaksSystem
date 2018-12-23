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
}, prefs = R.string.key_chrome_go_top_or_bottom)
public class ChromeGoTopOrBottom extends ChromeBase {

    @Override
    protected Map<String, MenuInfo> newMenus() {
        Map<String, MenuInfo> infos = new HashMap<>(2);
        infos.put(ContextUtils.getLString(R.string.chrome_go_top), new MenuInfo(1004, (activity, url, isCustomTab) -> {
            loadUrl(activity, "javascript:window.scrollTo(0, 0);");
        }));
        infos.put(ContextUtils.getLString(R.string.chrome_go_bottom), new MenuInfo(1005, (activity, url, isCustomTab) -> {
            loadUrl(activity, "javascript:window.scrollTo(0, document.body.scrollHeight);");
        }));
        return infos;
    }
}
