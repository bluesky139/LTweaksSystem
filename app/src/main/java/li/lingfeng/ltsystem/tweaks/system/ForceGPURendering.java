package li.lingfeng.ltsystem.tweaks.system;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = {}, prefs = R.string.key_display_force_gpu_rendering)
public class ForceGPURendering extends TweakBase {

    @Override
    public void android_view_Window__setWindowManager__WindowManager_IBinder_String_boolean(ILTweaks.MethodParam param) {
        param.before(() -> {
            String appName = (String) param.args[2];
            boolean hardwareAccelerated = (boolean) param.args[3];
            if (!hardwareAccelerated) {
                Logger.v("Set hardware accelerated for " + appName);
                param.setArg(3, true);
            }
        });
    }
}
