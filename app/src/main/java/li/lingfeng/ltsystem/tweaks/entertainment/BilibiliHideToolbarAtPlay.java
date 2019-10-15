package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.BILIBILI, prefs = R.string.key_bilibili_hide_toolbar_at_play)
public class BilibiliHideToolbarAtPlay extends TweakBase {

    private static final String VIDEO_DETAILS_ACTIVITY = "tv.danmaku.bili.ui.video.VideoDetailsActivity";

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(VIDEO_DETAILS_ACTIVITY, param, () -> {
            final Activity activity = (Activity) param.thisObject;
            ViewGroup appbar = (ViewGroup) ViewUtils.findViewByName(activity, "appbar");
            ViewGroup toolbar = (ViewGroup) ViewUtils.findViewByName(appbar, "nav_top_bar");
            View coverView = ViewUtils.findViewByName(appbar, "cover");
            View playButton = ViewUtils.findViewByName(appbar, "play");
            View.OnClickListener originalClickListener = ViewUtils.getViewClickListener(coverView);
            if (coverView == null || playButton == null || originalClickListener == null) {
                Logger.e("coverView " + coverView + ", playButton " + playButton + ", originalClickListener " + originalClickListener);
                return;
            }
            
            View.OnClickListener listener = v -> {
                originalClickListener.onClick(v);
                coverView.setOnClickListener(originalClickListener);
                try {
                    if (toolbar.getVisibility() == View.VISIBLE) {
                        Logger.v("Hide toolbar at play.");
                        toolbar.setVisibility(View.INVISIBLE);
                        ViewUtils.findViewByName(appbar, "shadow").setVisibility(View.INVISIBLE);
                    }

                    // Stop share icon animation.
                    ImageView iconView = (ImageView) ViewUtils.findViewByName(activity, "share_icon");
                    Drawable drawable = iconView.getDrawable();
                    new Handler().postDelayed(() -> {
                        Logger.v("Stop share icon animation.");
                        iconView.clearAnimation();
                        iconView.setImageDrawable(drawable);
                    }, 5000);
                } catch (Throwable e) {
                    Logger.e("coverView click exception.", e);
                }
            };
            coverView.setOnClickListener(listener);
            playButton.setOnClickListener(listener);
        });
    }
}
