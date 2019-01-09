package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.graphics.Color;
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
import li.lingfeng.ltsystem.utils.ReflectUtils;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.BILIBILI, prefs = R.string.key_bilibili_expand_desc)
public class BilibiliExpandDesc extends TweakBase {

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
                        final TextView desc = (TextView) ViewUtils.findViewByName(rootView, "desc");
                        if (desc != null && !desc.getText().toString().isEmpty()) {
                            new Handler().post(() -> {
                                try {
                                    handleDesc(rootView, desc);
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

    private void handleDesc(ViewGroup rootView, final TextView desc) throws Throwable {
        Logger.i("Expand desc.");
        desc.performClick();
        Object listenerInfo = ReflectUtils.callMethod(desc, "getListenerInfo");
        final View.OnClickListener listener = (View.OnClickListener) ReflectUtils.getObjectField(listenerInfo, "mOnClickListener");

        ViewGroup parent = (ViewGroup) desc.getParent();
        ViewUtils.traverseViews(parent, (view, deep) -> {
            view.setOnClickListener(null);
            return false;
        });
        parent.setOnClickListener(null);

        View arrow = ViewUtils.findViewByName(parent, "arrow");
        arrow.setOnClickListener((v) -> {
            Logger.i("Expand desc by arrow.");
            desc.setOnClickListener(listener);
            desc.performClick();
            desc.setOnClickListener(null);
        });

        // Set desc color to light a bit.
        desc.setTextColor(Color.parseColor("#A3A3A3"));
    }
}
