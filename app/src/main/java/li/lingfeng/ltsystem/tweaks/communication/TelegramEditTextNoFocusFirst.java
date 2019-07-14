package li.lingfeng.ltsystem.tweaks.communication;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.TELEGRAM, prefs = R.string.key_telegram_edittext_no_focus_first)
public class TelegramEditTextNoFocusFirst extends TweakBase {

    private static final String CHAT_ACTIVITY_ENTER_VIEW = "org.telegram.ui.Components.ChatActivityEnterView";

    @Override
    public void android_view_View__requestFocus__(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (param.thisObject.getClass().getName().startsWith(CHAT_ACTIVITY_ENTER_VIEW)) {
                StackTraceElement[] elements = Thread.currentThread().getStackTrace();
                for (int i = 0; i < Math.min(10, elements.length); ++i) {
                    StackTraceElement element = elements[i];
                    if (element.getClassName().equals("android.widget.TextView")) {
                        return;
                    }
                }
                Logger.v("EditText no focus without keyboard.");
                param.setResult(false);
            }
        });
    }
}
