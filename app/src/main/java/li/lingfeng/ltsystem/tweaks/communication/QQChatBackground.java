package li.lingfeng.ltsystem.tweaks.communication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;

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
    private static final String TOP_GESTURE_LAYOUT = "com.tencent.mobileqq.activity.fling.TopGestureLayout";
    private static final String CHAT_LISTVIEW = "com.tencent.mobileqq.bubble.ChatXListView";
    private static final int TITLE_COLOR = Color.parseColor("#00B1E9");
    private ViewGroup mTopGestureLayout;
    private boolean mInChatList = false;
    private BitmapDrawable mLargestDrawable;
    private LruCache<Integer, BitmapDrawable> mBackgroundDrawables; // height -> drawable, consider width is fixed.
    private long mLastModified = 0;
    private ViewGroup mBackgroundView;
    private View mTitlePrev;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(SPLASH_ACTIVITY, param, () -> {
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
            final ViewGroup rootView = activity.findViewById(android.R.id.content);
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                try {
                    if (mTopGestureLayout == null) {
                        mTopGestureLayout = (ViewGroup) ViewUtils.findViewByType(activity, findClass(TOP_GESTURE_LAYOUT));
                        if (mTopGestureLayout != null) {
                            Logger.d("mTopGestureLayout " + mTopGestureLayout);
                            mTopGestureLayout.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                                @Override
                                public void onChildViewAdded(View parent, View child) {
                                    Logger.d("mTopGestureLayout onChildViewAdded " + child);
                                    mBackgroundView = null;
                                    try {
                                        handleLayoutChanged((ViewGroup) child);
                                    } catch (Throwable e) {
                                        Logger.e("Error to handleLayoutChanged in onChildViewAdded.", e);
                                    }
                                }

                                @Override
                                public void onChildViewRemoved(View parent, View child) {
                                }
                            });
                        }
                    }

                    if (mInChatList && mBackgroundView == null) {
                        handleLayoutChanged(rootView);
                    }
                } catch (Throwable e) {
                    Logger.e("Error in root onGlobalLayout().", e);
                }
            });
        });
    }

    @Override
    public void android_view_View__setSystemUiVisibility__int(ILTweaks.MethodParam param) {
        param.before(() -> {
            mInChatList = ((int) param.args[0] & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) > 0;
            if (!mInChatList) {
                mBackgroundView = null;
                mTitlePrev = null;
            }
        });
    }

    @Override
    public void android_app_Activity__onDestroy__(ILTweaks.MethodParam param) {
        afterOnClass(SPLASH_ACTIVITY, param, () -> {
            mTopGestureLayout = null;
            mInChatList = false;
            mBackgroundView = null;
            mBackgroundDrawables = null;
            mLargestDrawable = null;
            mTitlePrev = null;
        });
    }

    private void handleLayoutChanged(ViewGroup rootView) throws Throwable {
        //Logger.d("rootView handleLayoutChanged");
        ViewGroup chatListView = ViewUtils.findViewByType(rootView, (Class<? extends View>) findClass(CHAT_LISTVIEW));
        if (chatListView == null) {
            return;
        }

        mBackgroundView = chatListView;
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

        View title = ViewUtils.findViewByName(rootView, "rlCommenTitle");
        title.setBackgroundColor(TITLE_COLOR);
        mTitlePrev = ViewUtils.prevView(title);
        mTitlePrev.setBackgroundColor(TITLE_COLOR);
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

    @Override
    public void android_view_View__setBackgroundColor__int(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (mTitlePrev == param.thisObject && (int) param.args[0] == 0xFFF6F7F9) {
                param.setResult(null);
            }
        });
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
}
