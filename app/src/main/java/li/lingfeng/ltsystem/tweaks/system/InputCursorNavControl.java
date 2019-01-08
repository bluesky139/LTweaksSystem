package li.lingfeng.ltsystem.tweaks.system;

import android.content.Context;
import android.provider.Settings;
import android.view.View;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.ANDROID_SYSTEM_UI, prefs = R.string.key_nav_bar_input_cursor_control)
public class InputCursorNavControl extends TweakBase {

    private boolean mVisible = false;

    @Override
    public void com_android_systemui_statusbar_phone_NavigationBarView__setDisabledFlags__int_boolean(ILTweaks.MethodParam param) {
        param.after(() -> {
            updateVisibility((View) param.thisObject);
        });
    }

    @Override
    public void com_android_systemui_statusbar_phone_NavigationBarView__setNavigationIconHints__int_boolean(ILTweaks.MethodParam param) {
        param.after(() -> {
            updateVisibility((View) param.thisObject);
        });
    }

    private void updateVisibility(View navBarView) throws Throwable {
        final int iconHints = ReflectUtils.getIntField(navBarView, "mNavigationIconHints");
        final int disabledFlags = ReflectUtils.getIntField(navBarView, "mDisabledFlags");
        final boolean visible = (disabledFlags & 0x01000000 /* STATUS_BAR_DISABLE_RECENT */) == 0
                && (iconHints & (1 << 0) /* NAVIGATION_HINT_BACK_ALT */) != 0;
        if (mVisible == visible) {
            return;
        }

        mVisible = visible;
        Context context = navBarView.getContext();
        if (mVisible) {
            Logger.d("Show nav cursor control.");
            String dpadLeft = "content://li.lingfeng.ltsystem.resourceProvider/raw/nav_dpad_left";
            String dpadRight = "content://li.lingfeng.ltsystem.resourceProvider/raw/nav_dpad_right";
            Settings.Secure.putString(context.getContentResolver(), "sysui_nav_bar",
                    "key(21:" + dpadLeft + ")[1.0],back[1.0];home[1.0];recent[1.0],key(22:" + dpadRight + ")[1.0]");
        } else {
            Logger.d("Hide nav cursor control.");
            Settings.Secure.putString(context.getContentResolver(), "sysui_nav_bar", null);
        }
    }
}
