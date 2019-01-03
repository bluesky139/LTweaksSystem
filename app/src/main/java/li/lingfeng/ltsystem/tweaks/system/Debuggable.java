package li.lingfeng.ltsystem.tweaks.system;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_debug_debuggable)
public class Debuggable extends TweakBase {

    @Override
    public void android_content_pm_PackageParser__parsePackage__File_int_boolean(ILTweaks.MethodParam param) {
        param.after(() -> {
            PackageParser.Package pkg = (PackageParser.Package) param.getResult();
            if (pkg == null) {
                return;
            }
            if (pkg.packageName.equals(PackageNames.ANDROID)) {
                Logger.i("Set debuggable for all apps.");
                return;
            }

            ApplicationInfo appInfo = pkg.applicationInfo;
            appInfo.flags |= ApplicationInfo.FLAG_DEBUGGABLE;
        });
    }
}
