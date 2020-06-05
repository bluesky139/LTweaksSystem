package li.lingfeng.ltsystem.tweaks.entertainment;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Field;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.BILIBILI, prefs = R.string.key_bilibili_hide_toolbar_at_play)
public class BilibiliHideToolbarAtPlay extends TweakBase {

    private static final String VIDEO_DETAILS_ACTIVITY = "tv.danmaku.bili.ui.video.VideoDetailsActivity";
    private static final String SHARE_ICON_VIEW = "tv.danmaku.bili.ui.video.section.DetailsShareAnimView";
    private ImageView mShareView;
    private Drawable mShareDrawable;
    private static Field sShareAnimatorField;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(VIDEO_DETAILS_ACTIVITY, param, () -> {
            final Activity activity = (Activity) param.thisObject;
            ViewGroup appbar = ViewUtils.findViewByName(activity, "appbar");
            ViewGroup toolbar = ViewUtils.findViewByName(appbar, "nav_top_bar");
            View videoView = (View) ViewUtils.findViewByName(activity, "play").getParent();
            View.OnClickListener originalClickListener = ViewUtils.getViewClickListener(videoView);
            if (videoView == null || originalClickListener == null) {
                Logger.e("videoView " + videoView + ", originalClickListener " + originalClickListener);
                return;
            }

            View.OnClickListener listener = v -> {
                videoView.setOnClickListener(originalClickListener);
                originalClickListener.onClick(v);
                try {
                    if (toolbar.getVisibility() == View.VISIBLE) {
                        Logger.v("Hide toolbar at play.");
                        toolbar.setVisibility(View.INVISIBLE);
                        ViewUtils.findViewByName(appbar, "shadow").setVisibility(View.INVISIBLE);
                    }

                    mShareView = (ImageView) ViewUtils.findViewByName(activity, "share_icon_new");
                    mShareDrawable = mShareView.getDrawable();
                    if (sShareAnimatorField == null) {
                        sShareAnimatorField = ReflectUtils.findFirstFieldByExactType(mShareView.getClass(), ValueAnimator.class);
                        Logger.d("sShareAnimatorField " + sShareAnimatorField);
                    }
                } catch (Throwable e) {
                    Logger.e("coverView click exception.", e);
                }
            };
            videoView.setOnClickListener(listener);
        });
    }

    @Override
    public void android_app_Activity__onDestroy__(ILTweaks.MethodParam param) {
        beforeOnClass(VIDEO_DETAILS_ACTIVITY, param, () -> {
            mShareView = null;
            mShareDrawable = null;
        });
    }

    @Override
    public void android_view_View__startAnimation__Animation(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (param.thisObject.getClass().getName().equals(SHARE_ICON_VIEW)) {
                Logger.v("Stop share icon animation.");
                param.setResult(null);
                mShareView = null;
            }
        });
    }

    @Override
    public void android_animation_ValueAnimator__start__(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (mShareView != null && sShareAnimatorField.get(mShareView) == param.thisObject) {
                Logger.v("Stop share icon animator.");
                param.setResult(null);
                mShareView.setImageDrawable(mShareDrawable);
                mShareView = null;
            }
        });
    }
}
