package li.lingfeng.ltsystem.tweaks.communication;

import android.app.Activity;
import android.content.Intent;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.Utils;

@MethodsLoad(packages = PackageNames.TIM, prefs = R.string.key_qq_outer_browser)
public class QQOuterBrowser extends TweakBase {

    private static final String BROWSER_DELEGATED_ACTIVITY = "com.tencent.mobileqq.activity.QQBrowserDelegationActivity";

    @Override
    public void android_app_Activity__startActivityForResult__Intent_int_Bundle(ILTweaks.MethodParam param) {
        param.before(() -> {
            Intent intent = (Intent) param.args[0];
            if (intent.getComponent() == null
                    || !intent.getComponent().getClassName().equals(BROWSER_DELEGATED_ACTIVITY)) {
                return;
            }

            Activity activity = (Activity) param.thisObject;
            String url = intent.getStringExtra("url");
            if (Utils.isUrl(url)) {
                Logger.i("QQ url " + url);
                ContextUtils.startBrowser(activity, url);
                param.setResult(null);
            }
        });
    }
}
