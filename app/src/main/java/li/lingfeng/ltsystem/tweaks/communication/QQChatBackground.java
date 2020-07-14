package li.lingfeng.ltsystem.tweaks.communication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.Handler;
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

@MethodsLoad(packages = PackageNames.QQ_LITE, prefs = R.string.key_qq_clear_background)
public class QQChatBackground extends TweakBase {

    private static final String SPLASH_ACTIVITY = "com.tencent.mobileqq.activity.SplashActivity";
    private static final String CHAT_ACTIVITY = "com.tencent.mobileqq.activity.ChatActivity";
    private static final String FORWARD_ACTIVITY = "com.tencent.mobileqq.activity.ForwardRecentActivity";
    private static final int TITLE_COLOR = Color.parseColor("#00B1E9");
    private BitmapDrawable mLargestDrawable;
    private LruCache<Integer, BitmapDrawable> mBackgroundDrawables; // height -> drawable, consider width is fixed.
    private long mLastModified = 0;
    private ViewGroup mBackgroundView;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(SPLASH_ACTIVITY, param, () -> {
            setTitleColor(param);
        });
        afterOnClass(FORWARD_ACTIVITY, param, () -> {
            setTitleColor(param);
        });
        afterOnClass(CHAT_ACTIVITY, param, () -> {
            File file = new File(getImagePath());
            if (!file.exists()) {
                Logger.w("Background image file doesn't exist, " + file.getAbsolutePath());
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
            final ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    boolean end = true;
                    try {
                        end = handleLayoutChanged(rootView);
                    } catch (Throwable e) {
                        Logger.e("handleLayoutChanged exception.", e);
                    }
                    if (end) {
                        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
            activity.getWindow().setStatusBarColor(TITLE_COLOR);
        });
    }

    private void setTitleColor(ILTweaks.MethodParam param) {
        final Activity activity = (Activity) param.thisObject;
        final ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                    View title = ViewUtils.findViewByName(rootView, "rl_title_bar");
                    if (title != null) {
                        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        title.setBackgroundColor(TITLE_COLOR);
                        for (int i = 1; i < rootView.getChildCount(); ++i) {
                            View child = rootView.getChildAt(i);
                            if (child.getClass() == View.class && child.getId() == View.NO_ID) {
                                child.setBackgroundColor(TITLE_COLOR);
                                break;
                            }
                        }
                        View statusBar = ViewUtils.findViewByName(rootView, "title_top_bar");
                        if (statusBar != null) {
                            statusBar.setBackgroundColor(TITLE_COLOR);
                        }
                    }
                } catch (Throwable e) {
                    Logger.e("onGlobalLayout exception.", e);
                }
            }
        });
    }

    private boolean handleLayoutChanged(ViewGroup rootView) throws Throwable {
        //Logger.d("rootView handleLayoutChanged");
        ViewGroup chatContent = (ViewGroup) ViewUtils.findViewByName(rootView, "chat_content");
        if (chatContent == null) {
            return false;
        }

        mBackgroundView = chatContent;
        if (mBackgroundView.getBackground() == null) {
            int measuredHeight = mBackgroundView.getMeasuredHeight();
            if (measuredHeight > 0) {
                int measuredWidth = mBackgroundView.getMeasuredWidth();
                Logger.d("Has measuredWidth " + measuredWidth + ", measuredHeight " + measuredHeight);
                updateChatBackground(measuredWidth, measuredHeight);
            }

            mBackgroundView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (v != mBackgroundView) {
                        // We only set layout change listener once, so we shouldn't remove listener if not in chat page.
                        if (mBackgroundView != null) {
                            v.removeOnLayoutChangeListener(this);
                        }
                        return;
                    }
                    if (right <= 0 || bottom <= 0) {
                        return;
                    }

                    //Logger.d("mBackgroundView onLayoutChange " + bottom + ", " + oldBottom);
                    if (bottom != oldBottom || mBackgroundView.getBackground() == null) {
                        int width = right - left;
                        int height = bottom - top;
                        updateChatBackground(width, height);
                    }
                }
            });
        }

        ViewGroup title = (ViewGroup) ViewUtils.findViewByName(rootView, "rlCommenTitle");
        title.setBackgroundColor(TITLE_COLOR);
        title.getChildAt(title.getChildCount() - 1).setBackgroundColor(TITLE_COLOR);
        new Handler().post(() -> {
            for (int i = 1; i < rootView.getChildCount(); ++i) {
                View child = rootView.getChildAt(i);
                if (child.getClass() == View.class && child.getId() == View.NO_ID) {
                    child.setBackgroundColor(TITLE_COLOR);
                    break;
                }
            }
        });
        return true;
    }

    private void updateChatBackground(int width, int height) {
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

        if (mBackgroundView.getBackground() != drawable) {
            Logger.i("Set chat activity background, " + width + "x" + height);
            mBackgroundView.setBackgroundDrawable(drawable);
        }
    }

    private void removeBackgroundDrawable(BitmapDrawable drawable) {
        if (mBackgroundView != null && mBackgroundView.getBackground() == drawable) {
            mBackgroundView.setBackground(null);
        }
        drawable.getBitmap().recycle();
    }

    private String getImagePath() {
        return Environment.getExternalStoragePublicDirectory("Tencent").getAbsolutePath()
                + "/ltweaks_qq_background";
    }

    @Override
    public void android_app_Activity__onDestroy__(ILTweaks.MethodParam param) {
        afterOnClass(CHAT_ACTIVITY, param, () -> {
            mBackgroundView = null;
        });
    }
}
