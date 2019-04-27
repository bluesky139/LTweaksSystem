package li.lingfeng.ltsystem.tweaks.shopping;

import android.content.Intent;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.JD, prefs = R.string.key_jd_ads)
public class JDAds extends TweakBase {

    private static final String AD_ACTIVITY = "com.jingdong.app.mall.ad.ADActivity";

    @Override
    public void android_app_Activity__startActivityForResult__Intent_int_Bundle(ILTweaks.MethodParam param) {
        param.before(() -> {
            Intent intent = (Intent) param.args[0];
            if (intent.getComponent() != null && intent.getComponent().getClassName().equals(AD_ACTIVITY)) {
                Logger.v("Ignore ADActivity");
                param.setResult(null);
            }
        });
    }
}
