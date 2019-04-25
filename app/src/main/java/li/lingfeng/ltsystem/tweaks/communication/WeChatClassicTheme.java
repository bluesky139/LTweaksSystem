package li.lingfeng.ltsystem.tweaks.communication;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.ClassNames;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.WE_CHAT, prefs = R.string.key_wechat_classic_theme)
public class WeChatClassicTheme extends TweakBase {

    private static final int COLOR = 0xFF303030;
    private static final int MAX_TOOLBAR_COUNT_IN_ACTIVITY = 2;
    private WeakHashMap<Activity, List<ViewGroup>> mActivityToolbars = new WeakHashMap<>(10);
    private WeakReference<TextView> mTitleTextView;
    private WeakHashMap<View, Void> mStatusBarBackgrounds = new WeakHashMap<>(5);

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        param.after(() -> {
            Activity activity = (Activity) param.thisObject;
            ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
            Class<? extends View> clsToolbar = (Class<? extends View>) findClass(ClassNames.TOOLBAR);
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    //Logger.d("onGlobalLayout " + activity);
                    try {
                        List<ViewGroup> toolbars = (List<ViewGroup>) ViewUtils.findAllViewByType(rootView, clsToolbar);
                        for (ViewGroup toolbar : toolbars) {
                            List<ViewGroup> handledToolbars = mActivityToolbars.get(activity);
                            if (handledToolbars == null) {
                                handledToolbars = new ArrayList<>(2);
                                mActivityToolbars.put(activity, handledToolbars);
                            }
                            if (!handledToolbars.contains(toolbar)) {
                                Logger.v("Handle toolbar " + toolbar + " in activity " + activity);
                                handleToolbar(toolbar);
                                handledToolbars.add(toolbar);
                            }
                            if (handledToolbars.size() >= MAX_TOOLBAR_COUNT_IN_ACTIVITY) {
                                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            }
                        }

                        View statusBarBackground = rootView.findViewById(android.R.id.statusBarBackground);
                        if (statusBarBackground != null) {
                            statusBarBackground.setBackgroundColor(COLOR);
                            ViewGroup viewGroup = (ViewGroup) statusBarBackground.getParent();
                            if (viewGroup.indexOfChild(statusBarBackground) != viewGroup.getChildCount() - 1) {
                                statusBarBackground.bringToFront();
                            }
                            mStatusBarBackgrounds.put(statusBarBackground, null);
                        }
                    } catch (Throwable e) {
                        Logger.e("Exception in classic theme global layout listener.", e);
                    }
                }
            });
        });
    }

    private void handleToolbar(ViewGroup toolbar) {
        toolbar.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            //Logger.d("toolbar " + toolbar + " layout change");
            try {
                toolbar.setBackgroundColor(COLOR);
                ViewUtils.traverseViews(toolbar, (view, deep) -> {
                    if (view.getVisibility() != View.VISIBLE) {
                        return false;
                    }
                    if (view instanceof ViewGroup) {
                        view.setBackgroundColor(COLOR);
                    } else if (view instanceof TextView) {
                        TextView textView = (TextView) view;
                        if (textView.getCurrentTextColor() != Color.WHITE) {
                            textView.setTextColor(Color.WHITE);
                            if (textView.getText().toString().startsWith("WeChat")) {
                                Logger.d("Title textview " + textView);
                                mTitleTextView = new WeakReference<>(textView);
                            }
                        }
                    } else if (view instanceof ImageView) {
                        Drawable drawable = ((ImageView) view).getDrawable();
                        if (drawable != null && drawable.getColorFilter() == null) {
                            drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                        }
                    }
                    return false;
                });
            } catch (Throwable e) {
                Logger.e("Handle toolbar " + toolbar + " exception.", e);
            }
        });
    }

    @Override
    public void com_android_internal_policy_PhoneWindow__setStatusBarColor__int(ILTweaks.MethodParam param) {
        param.before(() -> {
            param.setResult(null);
        });
    }

    @Override
    public void com_android_internal_policy_PhoneWindow__setNavigationBarColor__int(ILTweaks.MethodParam param) {
        param.before(() -> {
            param.setResult(null);
        });
    }

    @Override
    public void android_view_View__setSystemUiVisibility__int(ILTweaks.MethodParam param) {
        param.before(() -> {
            param.setArg(0, (int) param.args[0] & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        });
    }

    @Override
    public void android_widget_TextView__setTextColor__int(ILTweaks.MethodParam param) {
        param.before(() -> {
            TextView textView = mTitleTextView != null ? mTitleTextView.get() : null;
            if (param.thisObject == textView && Color.WHITE != (int) param.args[0]) {
                param.setResult(null);
            }
        });
    }

    @Override
    public void android_view_View__setBackgroundColor__int(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (mStatusBarBackgrounds.containsKey(param.thisObject) && (int) param.args[0] != COLOR) {
                param.setResult(null);
            }
        });
    }
}
