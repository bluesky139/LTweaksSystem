package li.lingfeng.ltsystem.tweaks.system;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;

@MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_system_share_direct_share_disable)
public class DirectShareDisable extends TweakBase {

    @Override
    public void com_android_internal_app_ChooserActivity__getAppPredictorForDirectShareIfEnabled__(ILTweaks.MethodParam param) {
        param.before(() -> {
            param.setResult(null);
        });
    }

    @Override
    public void com_android_internal_app_ChooserActivity__queryDirectShareTargets__ChooserListAdapter_boolean(ILTweaks.MethodParam param) {
        param.before(() -> {
            param.setResult(null);
        });
    }

    @Override
    public void com_android_internal_app_ChooserActivity__queryTargetServices__ChooserListAdapter(ILTweaks.MethodParam param) {
        param.before(() -> {
            param.setResult(null);
        });
    }

    @Override
    public void com_android_internal_app_ChooserActivity$ChooserListAdapter__getServiceTargetCount__(ILTweaks.MethodParam param) {
        param.before(() -> {
            param.setResult(0);
        });
    }

    @Override
    public void com_android_internal_app_ChooserActivity$ChooserListAdapter__getServiceTargetRowCount__(ILTweaks.MethodParam param) {
        param.before(() -> {
            param.setResult(0);
        });
    }
}
