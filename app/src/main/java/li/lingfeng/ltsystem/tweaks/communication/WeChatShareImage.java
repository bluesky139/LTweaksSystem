package li.lingfeng.ltsystem.tweaks.communication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;

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

@MethodsLoad(packages = PackageNames.WE_CHAT, prefs = R.string.key_wechat_share_image)
public class WeChatShareImage extends TweakBase {

    private static final String IMAGE_GALLERY_UI = "com.tencent.mm.ui.chatting.gallery.ImageGalleryUI";
    private static final String MM_VIEW_PAGER = "com.tencent.mm.ui.base.MMViewPager";
    private static final String MULTI_TOUCH_IMAGEVIEW = "com.tencent.mm.ui.base.MultiTouchImageView";
    private static final String SUBSAMPLING_SCALE_IMAGEVIEW = "com.davemorrissey.labs.subscaleview.view.SubsamplingScaleImageView";
    private static final int CORNER_DP = 80;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(IMAGE_GALLERY_UI, param, () -> {
            Activity activity = (Activity) param.thisObject;
            handleViewPager(activity);
        });
    }

    private void handleViewPager(Activity activity) throws Throwable {
        ViewGroup viewPager = (ViewGroup) ViewUtils.findViewByType(activity, findClass(MM_VIEW_PAGER));
        for (int i = 0; i < viewPager.getChildCount(); ++i) {
            ViewGroup child = (ViewGroup) viewPager.getChildAt(i);
            setTouchListeners(activity, child);
        }
        viewPager.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                try {
                    setTouchListeners(activity, (ViewGroup) child);
                } catch (Throwable e) {
                    Logger.e("setTouchListeners exception.", e);
                }
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
            }
        });
    }

    private void setTouchListeners(Activity activity, ViewGroup viewGroup) throws Throwable {
        setTouchListener(activity, viewGroup, MULTI_TOUCH_IMAGEVIEW);
        setTouchListener(activity, viewGroup, SUBSAMPLING_SCALE_IMAGEVIEW);
    }

    private void setTouchListener(Activity activity, ViewGroup viewGroup, String clsName) throws Throwable {
        View imageView = ViewUtils.findViewByType(viewGroup, findClass(clsName));
        if (imageView.getVisibility() == View.VISIBLE) {
            imageView.setOnTouchListener((v, event) -> {
                if (event.getX() > v.getWidth() - dp2px(CORNER_DP) && event.getY() < dp2px(CORNER_DP)) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        try {
                            Logger.i("Share image...");
                            Bitmap bitmap = v.getClass().getName().equals(MULTI_TOUCH_IMAGEVIEW)
                                    ? ((BitmapDrawable) ((ImageView) imageView).getDrawable()).getBitmap()
                                    : (Bitmap) ReflectUtils.callMethod(v, "getFullImageBitmap");
                            shareBitmap(activity, bitmap);
                        } catch (Throwable e) {
                            Logger.e("Share image exception.", e);
                        }
                    }
                    return true;
                }
                return false;
            });
        }
    }

    private void shareBitmap(Activity activity, Bitmap bitmap) throws Throwable {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        File file = new File("/sdcard/Tencent/ltweaks_share_image.png");
        FileUtils.writeByteArrayToFile(file, stream.toByteArray());
        ShareUtils.shareImage(activity, file);
    }
}
