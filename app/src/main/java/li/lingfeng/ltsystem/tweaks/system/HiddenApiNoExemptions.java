package li.lingfeng.ltsystem.tweaks.system;

import org.apache.commons.lang3.StringUtils;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.PackageUtils;

@MethodsLoad(packages = {}, excludedPackages = PackageNames.ANDROID, prefs = R.string.key_debug_hidden_api_no_exemptions)
public class HiddenApiNoExemptions extends TweakBase {

    @Override
    public void dalvik_system_VMRuntime__setHiddenApiExemptions__String$array(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (PackageUtils.isUserInstalledPackage(getPackageName())) {
                Logger.d("Hidden API no exemptions for " + getPackageName() + ", " + StringUtils.join(param.args, ", "));
                param.setResult(null);
            }
        });
    }
}
