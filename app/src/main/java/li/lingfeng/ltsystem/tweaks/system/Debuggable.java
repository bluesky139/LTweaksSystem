package li.lingfeng.ltsystem.tweaks.system;

import android.content.pm.ApplicationInfo;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_debug_debuggable)
public class Debuggable extends TweakBase {

    @Override
    public void com_android_server_am_ProcessList__startProcessLocked__String_ApplicationInfo_boolean_int_HostingRecord_boolean_boolean_int_boolean_String_String_String$array_Runnable(ILTweaks.MethodParam param) {
        param.before(() -> {
            ApplicationInfo info = (ApplicationInfo) param.args[1];
            if (info.packageName.equals(PackageNames.ANDROID)) {
                Logger.i("Set FLAG_DEBUGGABLE at startProcessLocked() for all apps.");
            } else {
                info.flags |= ApplicationInfo.FLAG_DEBUGGABLE;
            }
        });
    }
}
