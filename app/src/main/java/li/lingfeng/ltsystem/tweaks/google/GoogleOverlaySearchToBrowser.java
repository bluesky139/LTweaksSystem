package li.lingfeng.ltsystem.tweaks.google;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.LTHelper;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.GOOGLE, prefs = R.string.key_google_overlay_search_to_browser)
public class GoogleOverlaySearchToBrowser extends TweakBase {

    private static final String QUERY_ENTRY_ACTIVITY = "com.google.android.apps.gsa.queryentry.QueryEntryActivity";
    private static final String DYNAMIC_HOST_ACTIVITY = "com.google.android.apps.gsa.velour.dynamichosts.VelvetThemedDynamicHostActivity";

    @Override
    public void android_app_Activity__startActivityForResult__Intent_int_Bundle(final ILTweaks.MethodParam param) {
        beforeOnClass(QUERY_ENTRY_ACTIVITY, param, () -> {
            Intent intent = (Intent) param.args[0];
            if (intent.getComponent() != null && intent.getComponent().getClassName().equals(DYNAMIC_HOST_ACTIVITY)) {
                Object query = intent.getExtras().get("velvet-query");
                if (query != null) {
                    Pattern pattern = Pattern.compile("text from user: \"([^/]+)\"/");
                    Matcher matcher = pattern.matcher(query.toString());
                    if (matcher.find()) {
                        String url = "https://www.google.com/search?q=" + Uri.encode(matcher.group(1));
                        Activity activity = (Activity) param.thisObject;
                        ContextUtils.startBrowser(activity, url);
                        activity.finish();
                        param.setResult(null);
                    } else {
                        Logger.e("Can't find text from user.");
                    }
                } else {
                    Logger.e("Can't find velvet-query in intent.");
                }
            }
        });
    }
}
