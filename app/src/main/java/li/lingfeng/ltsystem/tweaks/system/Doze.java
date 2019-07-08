package li.lingfeng.ltsystem.tweaks.system;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_phone_doze)
public class Doze extends TweakBase {

    @Override
    public void com_android_server_DeviceIdleController__onStart__(ILTweaks.MethodParam param) {
        param.after(() -> {
            // config_enableAutoPowerModes
            Logger.i("DeviceIdleController enable auto power modes.");
            ReflectUtils.setBooleanField(param.thisObject, "mLightEnabled", true);
            ReflectUtils.setBooleanField(param.thisObject, "mDeepEnabled", true);
        });
    }
}
