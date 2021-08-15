package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ViewUtils;

public class BilibiliDisableSplash extends TweakBase {

    private static final String MAIN_ACTIVITY = "tv.danmaku.bili.MainActivityV2";

    @MethodsLoad(packages = PackageNames.BILIBILI, prefs = R.string.key_bilibili_disable_splash)
    public static class Bilibili extends TweakBase {

        @Override
        public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
            beforeOnClass(MAIN_ACTIVITY, param, () -> {
                Activity activity = (Activity) param.thisObject;
                ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
                rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        ViewGroup adView = ViewUtils.findViewByName(rootView, "splash_layout");
                        if (adView != null) {
                            Logger.d("hide splash_layout.");
                            adView.setVisibility(View.GONE);
                            adView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                                @Override
                                public void onChildViewAdded(View parent, View child) {
                                    Logger.d("remove " + child);
                                    ViewUtils.removeView(child);
                                }

                                @Override
                                public void onChildViewRemoved(View parent, View child) {
                                }
                            });
                        }

                        View splashView = ViewUtils.findViewByName(rootView, "splash_container");
                        if (splashView != null) {
                            Logger.d("hide splash_container.");
                            splashView.setVisibility(View.GONE);
                        }
                    }
                });
            });
        }
    }

    @MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_bilibili_disable_splash)
    public static class Android extends TweakBase {

        @Override
        public void com_android_internal_policy_PhoneWindow__generateLayout__DecorView(ILTweaks.MethodParam param) {
            param.before(() -> {
                Window window = (Window) param.thisObject;
                Context context = window.getContext();
                if (context.getPackageName().equals(PackageNames.BILIBILI)) {
                    Drawable drawable = ContextUtils.getColorDrawable("night", context);
                    if (drawable != null) {
                        Logger.i("Set night background for bilibili phone window.");
                        window.setBackgroundDrawable(drawable);
                    } else {
                        Logger.e("Can't set night backgorund for bilibili phone window.");
                    }
                }
            });
        }
    }
}
