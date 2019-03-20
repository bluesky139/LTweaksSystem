package li.lingfeng.ltsystem.tweaks.entertainment;

import android.content.ComponentName;
import android.content.pm.PackageManager;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.BILIBILI, prefs = R.string.key_bilibili_prevent_component_enabled)
public class BilibiliPreventComponentEnabled extends TweakBase {

    @Override
    public void android_app_ApplicationPackageManager__setComponentEnabledSetting__ComponentName_int_int(ILTweaks.MethodParam param) {
        param.before(() -> {
            int newState = (int) param.args[1];
            if (newState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                ComponentName componentName = (ComponentName) param.args[0];
                Logger.v("Prevent " + componentName + " to be enabled.");
                param.setResult(null);
            }
        });
    }
}
