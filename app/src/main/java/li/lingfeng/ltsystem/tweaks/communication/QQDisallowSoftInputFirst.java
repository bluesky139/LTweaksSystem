package li.lingfeng.ltsystem.tweaks.communication;

import android.view.View;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.TIM, prefs = R.string.key_qq_disallow_soft_input_first)
public class QQDisallowSoftInputFirst extends TweakBase {

    private static final String SPLASH_ACTIVITY = "com.tencent.mobileqq.activity.SplashActivity";
    private boolean mInMainActivity = false;
    private boolean mInChatList = false;

    @Override
    public void android_app_Activity__onResume__(ILTweaks.MethodParam param) {
        afterOnClass(SPLASH_ACTIVITY, param, () -> {
            mInMainActivity = true;
        });
    }

    @Override
    public void android_app_Activity__onPause__(ILTweaks.MethodParam param) {
        beforeOnClass(SPLASH_ACTIVITY, param, () -> {
            mInMainActivity = false;
        });
    }

    @Override
    public void android_view_View__setSystemUiVisibility__int(ILTweaks.MethodParam param) {
        param.before(() -> {
            mInChatList = ((int) param.args[0] & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) > 0;
        });
    }

    @Override
    public void android_view_inputmethod_InputMethodManager__showSoftInput__View_int_ResultReceiver(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (!mInMainActivity || !mInChatList) {
                return;
            }
            for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                if (element.getClassName().equals("android.widget.TextView")) {
                    return;
                }
            }
            Logger.v("Disallow showSoftInput without TextView in chat list.");
            param.setResult(false);
        });
    }
}
