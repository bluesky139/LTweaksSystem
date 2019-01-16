package li.lingfeng.ltsystem.tweaks.shopping;

import android.app.Activity;
import android.content.Intent;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.JD, prefs = R.string.key_jd_basic_share_activity)
public class JDBasicShare extends TweakBase {

    private static final String SHARE_ACTIVITY = "com.jingdong.app.mall.basic.ShareActivity";

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        beforeOnClass(SHARE_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            Intent intent = activity.getIntent();
            int action = intent.getIntExtra("action", 0);
            if (action != 1) {
                intent.putExtra("action", 1);
                Logger.i("ShareActivity action " + action + " -> 1");
            }
        });
    }
}
