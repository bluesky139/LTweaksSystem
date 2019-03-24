package li.lingfeng.ltsystem.tweaks.system;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_phone_doze_on_the_go)
public class DozeOnTheGo extends TweakBase {

    @Override
    public void com_android_server_DeviceIdleController__startMonitoringMotionLocked__(ILTweaks.MethodParam param) {
        param.before(() -> {
            Logger.v("Ignore DeviceIdleController.startMonitoringMotionLocked()");
            param.setResult(null);
        });
    }

    @Override
    public void com_android_server_DeviceIdleController__stopMonitoringMotionLocked__(ILTweaks.MethodParam param) {
        param.before(() -> {
            Logger.v("Ignore DeviceIdleController.stopMonitoringMotionLocked()");
            param.setResult(null);
        });
    }

    @Override
    public void com_android_server_DeviceIdleController__motionLocked__(ILTweaks.MethodParam param) {
        param.before(() -> {
            Logger.v("Ignore DeviceIdleController.motionLocked()");
            param.setResult(null);
        });
    }
}
