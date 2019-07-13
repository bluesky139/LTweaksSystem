package li.lingfeng.ltsystem.tweaks.communication;

import android.view.View;

import java.util.WeakHashMap;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.TELEGRAM, prefs = R.string.key_telegram_edittext_no_focus_first)
public class TelegramEditTextNoFocusFirst extends TweakBase {

    private static final String CHAT_ACTIVITY_ENTER_VIEW = "org.telegram.ui.Components.ChatActivityEnterView";
    private WeakHashMap<View, Void> mViews = new WeakHashMap<>();

    @Override
    public void android_view_View__requestFocus__(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (param.thisObject.getClass().getName().startsWith(CHAT_ACTIVITY_ENTER_VIEW)) {
                View view = (View) param.thisObject;
                if (!mViews.containsKey(view)) {
                    Logger.v("EditText no focus first.");
                    mViews.put(view, null);
                    param.setResult(null);
                }
            }
        });
    }

    @Override
    public void android_app_Activity__onResume__(ILTweaks.MethodParam param) {
        param.before(() -> {
            mViews.clear();
        });
    }
}
