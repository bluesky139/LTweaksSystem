package li.lingfeng.ltsystem.tweaks.system;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.ANDROID_SYSTEM_UI, prefs = R.string.key_nav_bar_input_cursor_control)
public class InputCursorNavControl extends TweakBase {

    private static final String KEY_BUTTON_DRAWABLE = "com.android.systemui.statusbar.policy.KeyButtonDrawable";
    private boolean mVisible = false;

    @Override
    public void com_android_systemui_statusbar_phone_NavigationBarInflaterView__createView__String_ViewGroup_LayoutInflater(ILTweaks.MethodParam param) {
        param.before(() -> {
            String buttonSpec = (String) param.args[0];
            ViewGroup parent = (ViewGroup) param.args[1];
            LayoutInflater inflater = (LayoutInflater) param.args[2];
            if (buttonSpec.startsWith("dpad_left")) {
                View view = createView(inflater, parent, "ltweaks_nav_dpad_left", 21);
                param.setResult(view);
            } else if (buttonSpec.startsWith("dpad_right")) {
                View view = createView(inflater, parent, "ltweaks_nav_dpad_right", 22);
                param.setResult(view);
            }
        });
    }

    private View createView(LayoutInflater inflater, ViewGroup parent, String drawableName, int code) throws Throwable {
        ImageView imageView = (ImageView) inflater.inflate(ContextUtils.getLayoutId("custom_key"), parent, false);
        Drawable drawable = (Drawable) ReflectUtils.callStaticMethod(findClass(KEY_BUTTON_DRAWABLE), "create",
                new Object[] { imageView.getContext(), ContextUtils.getDrawableId(drawableName), false },
                new Class[] { Context.class, int.class, boolean.class });
        imageView.setImageDrawable(drawable);
        ReflectUtils.callMethod(imageView, "setCode", new Object[] { code }, new Class[] { int.class });
        return imageView;
    }

    @Override
    public void com_android_systemui_statusbar_phone_NavigationBarView__setDisabledFlags__int(ILTweaks.MethodParam param) {
        param.after(() -> {
            updateVisibility((View) param.thisObject);
        });
    }

    @Override
    public void com_android_systemui_statusbar_phone_NavigationBarView__setNavigationIconHints__int(ILTweaks.MethodParam param) {
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
        if (mVisible) {
            Logger.d("Show nav cursor control.");
            setNavigationBarLayout(navBarView, "dpad_left[.5W],back[1WC];home;recent[1WC],dpad_right[.5W]");
        } else {
            Logger.d("Hide nav cursor control.");
            setNavigationBarLayout(navBarView, null);
        }
    }

    private void setNavigationBarLayout(View navBarView, String layoutValue) throws Throwable {
        Object navBarInflatorView = ReflectUtils.getObjectField(navBarView, "mNavigationInflaterView");
        ReflectUtils.setBooleanField(navBarInflatorView, "mUsingCustomLayout", layoutValue != null);;
        ReflectUtils.callMethod(navBarInflatorView, "clearViews");
        ReflectUtils.callMethod(navBarInflatorView, "inflateLayout", new Object[] { layoutValue }, new Class[] { String.class });
    }
}
