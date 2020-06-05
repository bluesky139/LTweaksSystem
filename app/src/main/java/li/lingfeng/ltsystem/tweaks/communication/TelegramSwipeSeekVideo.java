package li.lingfeng.ltsystem.tweaks.communication;

import android.graphics.Color;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;
import li.lingfeng.ltsystem.utils.ViewUtils;

import static li.lingfeng.ltsystem.utils.ContextUtils.dp2px;

@MethodsLoad(packages = PackageNames.TELEGRAM, prefs = R.string.key_telegram_swipe_seek_video)
public class TelegramSwipeSeekVideo extends TweakBase {

    private static final String PHOTO_VIEWER = "org.telegram.ui.PhotoViewer";
    private static final String PHOTO_VIEWER_FRAMELAYOUT = PHOTO_VIEWER + "$FrameLayoutDrawer";
    private float mScrollDistance = 0f;
    private TextView mSeekTextView;

    @Override
    public void android_view_WindowManagerImpl__addView__View_ViewGroup$LayoutParams(ILTweaks.MethodParam param) {
        param.after(() -> {
            final View view = (View) param.args[0];
            if (view.getClass().getName().startsWith(PHOTO_VIEWER)) {
                new Handler().post(() -> {
                    try {
                        setVideoTouchListener(view);
                    } catch (Throwable e) {
                        Logger.e("setVideoTouchListener exception.", e);
                    }
                });
            }
        });
    }

    private void setVideoTouchListener(final View view) throws Throwable {
        final Object photoViewer = ReflectUtils.getSurroundingThis(view);
        final Object videoPlayer = ReflectUtils.getObjectField(photoViewer, "videoPlayer");
        if (videoPlayer == null) {
            view.setOnTouchListener(null);
            return;
        }
        final GestureDetector gestureDetector = new GestureDetector(view.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                try {
                    mScrollDistance += distanceX;
                    if (Math.abs(mScrollDistance) >= dp2px(12)) {
                        showSeekText(view);
                    }
                } catch (Throwable e) {
                    Logger.e("onScroll exception.", e);
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent event) {
                try {
                    if ((boolean) ReflectUtils.callMethod(videoPlayer, "isPlaying")) {
                        Logger.v("Pause.");
                        ReflectUtils.callMethod(videoPlayer, "pause");
                    } else {
                        Logger.v("Play.");
                        ReflectUtils.callMethod(videoPlayer, "play");
                    }
                } catch (Throwable e) {
                    Logger.e("Play/Pause exception.", e);
                }
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                try {
                    boolean isActionBarVisible = ReflectUtils.getBooleanField(photoViewer, "isActionBarVisible");
                    Logger.v("toggleActionBar " + !isActionBarVisible);
                    ReflectUtils.callMethod(photoViewer, "toggleActionBar",
                            new Object[] { !isActionBarVisible, true }, new Class[] { boolean.class, boolean.class });
                } catch (Throwable e) {
                    Logger.e("toggleActionBar exception.", e);
                }
                return true;
            }
        });

        view.setOnTouchListener((v, event) -> {
            if (gestureDetector.onTouchEvent(event)) {
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                try {
                    if (Math.abs(mScrollDistance) < dp2px(12)) {
                        return true;
                    }
                    hideSeekText();
                    long second = -(long)(mScrollDistance / view.getWidth() * 90);
                    mScrollDistance = 0f;
                    if (second == 0) {
                        return true;
                    }
                    long pos = (long) ReflectUtils.callMethod(videoPlayer, "getCurrentPosition");
                    long newPos = pos + second * 1000L;
                    Logger.v("Seek from " + pos + " to " + newPos);
                    ReflectUtils.callMethod(videoPlayer, "seekTo", new Object[] { newPos }, new Class[] { long.class });
                } catch (Throwable e) {
                    Logger.e("Seek exception.", e);
                }
            }
            return true;
        });

        ReflectUtils.callMethod(videoPlayer, "setLooping", new Object[] { false }, new Class[] { boolean.class });
    }

    private void showSeekText(View view) throws Throwable {
        if (mSeekTextView == null) {
            View view2 = ViewUtils.findViewByType((ViewGroup) view, findClass(PHOTO_VIEWER_FRAMELAYOUT));
            mSeekTextView = new TextView(view.getContext());
            mSeekTextView.setTextSize(26);
            mSeekTextView.setTextColor(Color.LTGRAY);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
            ((ViewGroup) view2).addView(mSeekTextView, layoutParams);
        }
        int second = -(int)(mScrollDistance / view.getWidth() * 90);
        mSeekTextView.setText("[" + (second > 0 ? "+" : "") + second + "]");
    }

    private void hideSeekText() throws Throwable {
        if (mSeekTextView != null) {
            ViewUtils.removeView(mSeekTextView);
            mSeekTextView = null;
        }
    }
}
