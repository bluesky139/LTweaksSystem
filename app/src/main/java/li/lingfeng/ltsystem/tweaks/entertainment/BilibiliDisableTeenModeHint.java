package li.lingfeng.ltsystem.tweaks.entertainment;

import android.content.Intent;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.BILIBILI, prefs = R.string.key_bilibili_disable_teen_mode_hint)
public class BilibiliDisableTeenModeHint extends TweakBase {

    private static final String TEEN_MODE_DIALOG_ACTIVITY = "com.bilibili.teenagersmode.ui.TeenagersModeDialogActivity";

    @Override
    public void android_app_Activity__startActivityForResult__Intent_int_Bundle(ILTweaks.MethodParam param) {
        param.before(() -> {
            Intent intent = (Intent) param.args[0];
            if (intent.getComponent() != null && intent.getComponent().getClassName().equals(TEEN_MODE_DIALOG_ACTIVITY)) {
                Logger.v("Disable teen mode dialog hint.");
                param.setResult(null);
            }
        });
    }
}
