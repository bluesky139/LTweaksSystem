package li.lingfeng.ltsystem.tweaks.communication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import java.io.File;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.IOUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.TIM, prefs = R.string.key_qq_clear_background)
public class QQChatBackground extends TweakBase {

    private static final String SPLASH_ACTIVITY = "com.tencent.mobileqq.activity.SplashActivity";
    private static final String CHAT_LISTVIEW = "com.tencent.mobileqq.bubble.ChatXListView";
    private BitmapDrawable mLargestDrawable;
    private LruCache<Integer, BitmapDrawable> mBackgroundDrawables; // height -> drawable, consider width is fixed.
    private long mLastModified = 0;
    private ViewGroup mBackgroundView;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(SPLASH_ACTIVITY, param, () -> {
            File file = new File(getImagePath());
            if (!file.exists()) {
                Logger.i("Background image file doesn't exist, " + file.getAbsolutePath());
                return;
            }

            if (mBackgroundDrawables == null) {
                mBackgroundDrawables = new LruCache<Integer, BitmapDrawable>(5) {
                    @Override
                    protected void entryRemoved(boolean evicted, Integer key, BitmapDrawable oldValue, BitmapDrawable newValue) {
                        Logger.d("entryRemoved " + key);
                        removeBackgroundDrawable(oldValue);
                    }
                };
            }
            if (mLastModified != file.lastModified()) {
                mLastModified = file.lastModified();
                if (mLargestDrawable != null) {
                    removeBackgroundDrawable(mLargestDrawable);
                    mLargestDrawable = null;
                }
                mBackgroundDrawables.evictAll();
            }

            final Activity activity = (Activity) param.thisObject;
            final ViewGroup rootView = (ViewGroup) activity.findViewById(android.R.id.content);
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    try {
                        handleLayoutChanged(activity);
                    } catch (Throwable e) {
                        Logger.e("Error to handleLayoutChanged.", e);
                    }
                    if (mBackgroundView != null) {
                        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mBackgroundView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                            try {
                                handleLayoutChanged(activity);
                            } catch (Throwable e) {
                                Logger.e("Error to handleLayoutChanged2.", e);
                            }
                        });
                    }
                }
            });
        });
    }

    @Override
    public void android_app_Activity__onDestroy__(ILTweaks.MethodParam param) {
        afterOnClass(SPLASH_ACTIVITY, param, () -> {
            mBackgroundView = null;
            mBackgroundDrawables = null;
            mLargestDrawable = null;
        });
    }

    private void handleLayoutChanged(Activity activity) throws Throwable {
        ViewGroup viewGroup = mBackgroundView;
        if (viewGroup == null) {
            final ViewGroup rootView = activity.findViewById(android.R.id.content);
            View chatListView = ViewUtils.findViewByType(rootView, (Class<? extends View>) findClass(CHAT_LISTVIEW));
            if (chatListView == null) {
                return;
            }
            viewGroup = (ViewGroup) chatListView;
        }

        int width = viewGroup.getMeasuredWidth();
        int height = viewGroup.getMeasuredHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        BitmapDrawable drawable = null;
        if (mLargestDrawable != null && mLargestDrawable.getBitmap().getHeight() == height) {
            drawable = mLargestDrawable;
        }
        if (drawable == null) {
            drawable = mBackgroundDrawables.get(height);
        }

        if (drawable == null) {
            if (mLargestDrawable != null && mLargestDrawable.getBitmap().getHeight() > height) {
                Bitmap bitmap = IOUtils.bitmapCopy(mLargestDrawable.getBitmap(), 0, 0, width, height);
                if (bitmap == null) {
                    return;
                }
                drawable = new BitmapDrawable(bitmap);
                mBackgroundDrawables.put(height, drawable);
            }

            if (drawable == null) {
                mBackgroundDrawables.evictAll();
                if (mLargestDrawable != null) {
                    removeBackgroundDrawable(mLargestDrawable);
                    mLargestDrawable = null;
                }
                String filepath = getImagePath();
                if (!new File(filepath).exists()) {
                    Logger.e("Can't access file " + filepath);
                    return;
                }
                Bitmap dest = IOUtils.createCenterCropBitmapFromFile(filepath, width, height);
                if (dest == null) {
                    return;
                }
                drawable = new BitmapDrawable(dest);
                mLargestDrawable = drawable;
            }
        }

        if (viewGroup.getBackground() != drawable) {
            Logger.i("Set chat activity background, " + width + "x" + height);
            viewGroup.setBackgroundDrawable(drawable);
            if (mBackgroundView == null) {
                mBackgroundView = viewGroup;
            }
        }
    }

    private void removeBackgroundDrawable(BitmapDrawable drawable) {
        if (mBackgroundView != null && mBackgroundView.getBackground() == drawable) {
            mBackgroundView.setBackgroundColor(Color.TRANSPARENT);
        }
        drawable.getBitmap().recycle();
    }

    private String getImagePath() {
        return Environment.getExternalStoragePublicDirectory("Tencent").getAbsolutePath()
                + "/ltweaks_qq_background";
    }
}
