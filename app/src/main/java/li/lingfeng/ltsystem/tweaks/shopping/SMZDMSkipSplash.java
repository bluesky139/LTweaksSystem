package li.lingfeng.ltsystem.tweaks.shopping;

import android.app.Activity;
import android.content.Intent;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.SMZDM, prefs = R.string.key_smzdm_skip_splash)
public class SMZDMSkipSplash extends TweakBase {

    private static final String WELCOME_ACTIVITY = "com.smzdm.client.android.app.WelComeActivity";
    private static final String HOME_ACTIVITY = "com.smzdm.client.android.app.HomeActivity";

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(WELCOME_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            if (!activity.isFinishing()) {
                Logger.v("Skip WelComeActivity.");
                Intent intent = new Intent();
                intent.setClassName(PackageNames.SMZDM, HOME_ACTIVITY);
                activity.startActivity(intent);
                activity.finish();
            }
        });
    }
}
