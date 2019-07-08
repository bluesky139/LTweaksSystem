package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.BILIBILI, prefs = R.string.key_bilibili_get_cover)
public class BilibiliCover extends TweakBase {

    private static final String VIDEO_DETAILS_ACTIVITY = "tv.danmaku.bili.ui.video.VideoDetailsActivity";
    private static final String VIDEO_DETAIL = "tv.danmaku.bili.ui.video.api.BiliVideoDetail";

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(VIDEO_DETAILS_ACTIVITY, param, () -> {
            final Activity activity = (Activity) param.thisObject;
            ViewGroup toolbar = (ViewGroup) ViewUtils.findViewByName(activity, "nav_top_bar");
            ImageView overflowImageView = (ImageView) ViewUtils.findViewByName(toolbar, "overflow");
            LinearLayout.LayoutParams overflowLayoutParams = (LinearLayout.LayoutParams) overflowImageView.getLayoutParams();

            ImageView imageView = new ImageView(activity);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    overflowLayoutParams.width, overflowLayoutParams.height);
            layoutParams.weight = overflowLayoutParams.weight;
            layoutParams.gravity = overflowLayoutParams.gravity;
            imageView.setLayoutParams(layoutParams);
            imageView.setScaleType(overflowImageView.getScaleType());
            Drawable drawable = ContextUtils.getDrawable("sobot_uploadpicture");
            drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            imageView.setImageDrawable(drawable);
            imageView.setBackground(overflowImageView.getBackground());

            LinearLayout parent = (LinearLayout) overflowImageView.getParent();
            parent.addView(imageView, parent.indexOfChild(overflowImageView));
            imageView.setOnClickListener((v) -> {
                try {
                    GetCover(activity);
                } catch (Throwable e) {
                    Logger.e("Can't get cover, " + e);
                    Logger.stackTrace(e);
                    Toast.makeText(activity, ContextUtils.getLString(R.string.error), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void GetCover(Activity activity) throws Throwable {
        Field field = ReflectUtils.findFirstFieldByExactType(activity.getClass(), findClass(VIDEO_DETAIL));
        Object videoDetail = field.get(activity);
        String cover = (String) ReflectUtils.getObjectField(videoDetail, "mCover");
        if (StringUtils.isEmpty(cover)) {
            throw new Exception("Empty cover url.");
        }
        ContextUtils.startBrowser(activity, cover);
    }
}
