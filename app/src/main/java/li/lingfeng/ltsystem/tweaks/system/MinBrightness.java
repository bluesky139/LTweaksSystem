package li.lingfeng.ltsystem.tweaks.system;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = {
        PackageNames.ANDROID,
        PackageNames.ANDROID_SETTINGS,
        PackageNames.ANDROID_SETTINGS_PROVIDER,
        PackageNames.ANDROID_SYSTEM_UI
}, prefs = {})
public class MinBrightness extends TweakBase {

    @Override
    public void android_os_PowerManager__getMinimumScreenBrightnessSetting__(ILTweaks.MethodParam param) {
        param.before(() -> {
            int minBrightness = Prefs.instance().getInt(R.string.key_display_min_brightness, 0);
            if (minBrightness <= 0) {
                return;
            }
            Logger.i("Return min brightness " + minBrightness);
            param.setResult(minBrightness);
        });
    }
}
