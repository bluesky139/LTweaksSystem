package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.BILIBILI, prefs = R.string.key_bilibili_hide_follow_at_full_screen)
public class BilibiliHideFollowAtFullScreen extends TweakBase {

    private static final String VIDEO_DETAILS_ACTIVITY = "tv.danmaku.bili.ui.video.VideoDetailsActivity";

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(VIDEO_DETAILS_ACTIVITY, param, () -> {
            final Activity activity = (Activity) param.thisObject;
            final ViewGroup rootView = (ViewGroup) activity.findViewById(android.R.id.content);
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    boolean end = false;
                    try {
                        end = hookControllerPage(rootView);
                    } catch (Throwable e) {
                        end = true;
                        Logger.e("Can't hook controller page, " + e);
                        Logger.stackTrace(e);
                    }
                    if (end) {
                        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        });
    }

    private boolean hookControllerPage(ViewGroup rootView) {
        final ViewGroup controllerPage = (ViewGroup) ViewUtils.findViewByName(rootView, "controller_page");
        if (controllerPage == null) {
            return false;
        }
        Logger.d("controllerPage " + controllerPage);

        controllerPage.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            View avatarLayout = ViewUtils.findViewByName(controllerPage, "avatar_layout");
            if (avatarLayout != null && avatarLayout.getVisibility() == View.VISIBLE) {
                Logger.i("Hide follow button at full screen.");
                avatarLayout.setVisibility(View.GONE);
            }
        });
        return true;
    }
}
