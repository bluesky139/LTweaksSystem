package li.lingfeng.ltsystem.tweaks.shopping;

import android.content.Intent;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.TAOBAO, prefs = R.string.key_taobao_ads)
public class TaobaoAds extends TweakBase {

    private static final String BOOT_IMAGE_ACTIVITY = "com.taobao.bootimage.activity.BootImageActivity";

    @Override
    public void android_app_Activity__startActivityForResult__Intent_int_Bundle(ILTweaks.MethodParam param) {
        param.before(() -> {
            Intent intent = (Intent) param.args[0];
            // action: action.fill.splash.content
            if (intent.getComponent() != null && intent.getComponent().getClassName().equals(BOOT_IMAGE_ACTIVITY)) {
                Logger.v("Ignore BootImageActivity");
                param.setResult(null);
            }
        });
    }
}
