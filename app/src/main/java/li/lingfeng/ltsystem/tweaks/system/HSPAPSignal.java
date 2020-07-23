package li.lingfeng.ltsystem.tweaks.system;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.ANDROID_SYSTEM_UI, prefs = R.string.key_display_hspap_signal)
public class HSPAPSignal extends TweakBase {

    @Override
    public void com_android_systemui_statusbar_policy_NetworkControllerImpl$Config__readConfig__Context(ILTweaks.MethodParam param) {
        param.after(() -> {
            Logger.i("Set hspaDataDistinguishable to true.");
            ReflectUtils.setBooleanField(param.getResult(), "hspaDataDistinguishable", true);
        });
    }
}
