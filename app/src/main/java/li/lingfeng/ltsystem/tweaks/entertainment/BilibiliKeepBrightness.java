package li.lingfeng.ltsystem.tweaks.entertainment;

import android.view.WindowManager;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.BILIBILI, prefs = R.string.key_bilibili_keep_brightness)
public class BilibiliKeepBrightness extends TweakBase {

    @Override
    public void com_android_internal_policy_PhoneWindow__setAttributes__WindowManager$LayoutParams(ILTweaks.MethodParam param) {
        param.before(() -> {
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) param.args[0];
            if (params.screenBrightness != WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
                Logger.v("Keep current brightness instead of " + params.screenBrightness);
                params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            }
        });
    }
}
