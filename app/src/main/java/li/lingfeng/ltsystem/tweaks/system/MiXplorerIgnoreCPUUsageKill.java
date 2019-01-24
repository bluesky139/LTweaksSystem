package li.lingfeng.ltsystem.tweaks.system;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_mixplorer_ignore_cpu_usage_kill)
public class MiXplorerIgnoreCPUUsageKill extends TweakBase {

    @Override
    public void com_android_server_am_ProcessRecord__kill__String_boolean(ILTweaks.MethodParam param) {
        param.before(() -> {
            String processName = (String) ReflectUtils.getObjectField(param.thisObject, "processName");
            if (PackageNames.MIXPLORER.equals(processName)) {
                for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                    if (element.getMethodName().equals("checkExcessivePowerUsageLocked")) {
                        Logger.i("com.mixplorer should be killed by \"" + param.args[0] + "\", but keep it.");
                        param.setResult(null);
                        return;
                    }
                }
            }
        });
    }
}
