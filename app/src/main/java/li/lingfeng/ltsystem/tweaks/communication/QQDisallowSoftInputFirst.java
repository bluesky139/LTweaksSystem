package li.lingfeng.ltsystem.tweaks.communication;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.TIM, prefs = R.string.key_qq_disallow_soft_input_first)
public class QQDisallowSoftInputFirst extends TweakBase {

    @Override
    public void android_view_inputmethod_InputMethodManager__showSoftInput__View_int_ResultReceiver(ILTweaks.MethodParam param) {
        param.before(() -> {
            for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                if (element.getClassName().equals("android.widget.TextView")) {
                    return;
                }
            }
            Logger.v("Disallow showSoftInput without TextView.");
            param.setResult(false);
        });
    }
}
