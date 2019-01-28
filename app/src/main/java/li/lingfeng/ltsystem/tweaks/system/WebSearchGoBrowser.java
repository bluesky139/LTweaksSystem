package li.lingfeng.ltsystem.tweaks.system;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ContextUtils;

@MethodsLoad(packages = {}, prefs = R.string.key_web_search_to_browser, excludedPackages = PackageNames.ANDROID)
public class WebSearchGoBrowser extends TweakBase {

    @Override
    public void android_app_Activity__startActivityForResult__Intent_int_Bundle(ILTweaks.MethodParam param) {
        param.before(() -> {
            Intent intent = (Intent) param.args[0];
            if (Intent.ACTION_WEB_SEARCH.equals(intent.getAction())) {
                String query = intent.getStringExtra("query");
                String url = "https://www.google.com/search?q=" + Uri.encode(query);
                Activity activity = (Activity) param.thisObject;
                ContextUtils.startBrowser(activity, url);
                param.setResult(null);
            }
        });
    }
}
