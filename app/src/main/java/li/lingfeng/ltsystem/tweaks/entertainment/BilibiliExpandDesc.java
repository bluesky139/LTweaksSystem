package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.BILIBILI, prefs = R.string.key_bilibili_expand_desc)
public class BilibiliExpandDesc extends TweakBase {

    private static final String VIDEO_DETAILS_ACTIVITY = "com.bilibili.video.videodetail.VideoDetailsActivity";

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(VIDEO_DETAILS_ACTIVITY, param, () -> {
            final Activity activity = (Activity) param.thisObject;
            final ViewGroup rootView = activity.findViewById(android.R.id.content);
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    boolean end = false;
                    try {
                        final View arrow = ViewUtils.findViewByName(rootView, "arrow");
                        if (arrow != null) {
                            new Handler().post(() -> {
                                try {
                                    Logger.i("Expand desc.");
                                    ViewGroup parent = (ViewGroup) arrow.getParent();
                                    parent.performClick();
                                    TextView desc = ViewUtils.findViewByName(parent, "desc");
                                    desc.setTextColor(0xFFA3A3A3);
                                } catch (Throwable e) {
                                    Logger.e("handleDesc error, " + e);
                                }
                            });
                            end = true;
                        }
                    } catch (Throwable e) {
                        Logger.e("Find desc error, " + e);
                        end = true;
                    } finally {
                        if (end) {
                            rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                }
            });
        });
    }
}
