package li.lingfeng.ltsystem.tweaks.system;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Binder;

import org.apache.commons.lang3.ArrayUtils;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.ANDROID, prefs = {})
public class PreventComponentEnabled extends PreventRunning {

    @Override
    protected int getPreventListKey() {
        return R.string.key_prevent_list_prevent_component_enabled;
    }

    @Override
    public void com_android_server_pm_PackageManagerService__setComponentEnabledSetting__ComponentName_int_int_int(ILTweaks.MethodParam param) {
        param.before(() -> {
            int callingUid = Binder.getCallingUid();
            if (ArrayUtils.contains(getPreventUids(), callingUid) && (int) param.args[1] == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                ComponentName componentName = (ComponentName) param.args[0];
                Logger.v("Prevent " + componentName + " to be enabled.");
                param.setResult(null);
            }
        });
    }
}
