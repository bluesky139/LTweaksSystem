package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

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
            ViewGroup toolbar = (ViewGroup) ViewUtils.findViewByName(activity, "nav_top_bar");
            View coverView = ViewUtils.findViewByName(activity, "cover");
            View.OnClickListener originalClickListener = ViewUtils.getViewClickListener(coverView);
            if (coverView == null || originalClickListener == null) {
                Logger.e("coverView " + coverView + ", originalClickListener " + originalClickListener);
                return;
            }
            coverView.setOnClickListener((v) -> {
                originalClickListener.onClick(v);
                if (toolbar.getVisibility() == View.VISIBLE) {
                    Logger.v("Hide toolbar at play.");
                    toolbar.setVisibility(View.INVISIBLE);
                }
                coverView.setOnClickListener(originalClickListener);
            });
        });
    }
}
