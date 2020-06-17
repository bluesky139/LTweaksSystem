package li.lingfeng.ltsystem.tweaks.communication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;
import li.lingfeng.ltsystem.utils.ShareUtils;
import li.lingfeng.ltsystem.utils.ViewUtils;

import static li.lingfeng.ltsystem.utils.ContextUtils.dp2px;

@MethodsLoad(packages = PackageNames.QQ_LITE, prefs = R.string.key_qq_share_image)
public class QQShareImage extends TweakBase {

    private static final String GALLERY_ACTIVITY = "com.tencent.mobileqq.activity.aio.photo.AIOGalleryActivity";
    private static final int CORNER_DP = 80;
    private ImageView mImageView;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(GALLERY_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            ViewGroup viewGroup = (ViewGroup) ViewUtils.findViewByName(activity, "gallery");
            viewGroup.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                @Override
                public void onChildViewAdded(View parent, View child) {
                    if (child instanceof ImageView) {
                        mImageView = (ImageView) child;
                    } else {
                        Logger.w("gallery child is not ImageView.");
                    }
                }

                @Override
                public void onChildViewRemoved(View parent, View child) {
                }
            });

            viewGroup.setOnTouchListener((v, event) -> {
                if (event.getX() > v.getWidth() - dp2px(CORNER_DP) && event.getY() < dp2px(CORNER_DP)) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        try {
                            Logger.i("Share image...");
                            Drawable drawable = mImageView.getDrawable();
                            drawable = (Drawable) ReflectUtils.callMethod(drawable, "getCurrDrawable");
                            Bitmap bitmap = (Bitmap) ReflectUtils.callMethod(drawable, "getBitmap");
                            ShareUtils.shareImage(activity, bitmap);
                        } catch (Throwable e) {
                            Logger.e("Share image exception.", e);
                        }
                    }
                    return true;
                }
                return false;
            });
        });
    }

    @Override
    public void android_app_Activity__onDestroy__(ILTweaks.MethodParam param) {
        beforeOnClass(GALLERY_ACTIVITY, param, () -> {
            mImageView = null;
        });
    }
}
