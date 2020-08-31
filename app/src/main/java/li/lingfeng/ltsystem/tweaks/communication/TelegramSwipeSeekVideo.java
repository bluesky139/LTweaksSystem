package li.lingfeng.ltsystem.tweaks.communication;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

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

@MethodsLoad(packages = PackageNames.TELEGRAM, prefs = R.string.key_telegram_swipe_seek_video)
public class TelegramSwipeSeekVideo extends TweakBase {

    private static final String PHOTO_VIEWER = "org.telegram.ui.PhotoViewer";
    private static final String PHOTO_VIEWER_FRAMELAYOUT = PHOTO_VIEWER + "$FrameLayoutDrawer";
    private static final String ACTION_BAR_MENU = "org.telegram.ui.ActionBar.ActionBarMenu";
    private static final int CORNER_DP = 80;
    private float mScrollDistance = 0f;
    private TextView mSeekTextView;
    private ImageButton mPlayButton;
    private Drawable mTransparentPlayDrawable;
    private Drawable mTransparentPauseDrawable;
    private boolean mIsPlaying;
    private boolean mInVideoViewer = false;

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
                playPause(videoPlayer);
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                try {
                    if (event.getX() > view.getWidth() - dp2px(CORNER_DP) && event.getY() < dp2px(CORNER_DP)) {
                        Logger.i("Share image from video.");
                        TextureView videoTextureView = (TextureView) ReflectUtils.getObjectField(photoViewer, "videoTextureView");
                        Bitmap bitmap = videoTextureView.getBitmap();
                        ShareUtils.shareImage(view.getContext(), bitmap);
                        return true;
                    }

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

        if (mPlayButton == null) {
            Drawable[] progressDrawables = (Drawable[]) ReflectUtils.getObjectField(photoViewer, "progressDrawables");
            Drawable playDrawable = progressDrawables[3];
            Drawable pauseDrawable = progressDrawables[4];
            progressDrawables[3] = mTransparentPlayDrawable = new ColorDrawable(Color.TRANSPARENT);
            progressDrawables[4] = mTransparentPauseDrawable = new ColorDrawable(Color.TRANSPARENT);

            LevelListDrawable levelDrawable = new LevelListDrawable();
            levelDrawable.addLevel(3, 3, playDrawable);
            levelDrawable.addLevel(4, 4, pauseDrawable);

            mPlayButton = new ImageButton(view.getContext());
            mPlayButton.setBackgroundColor(Color.TRANSPARENT);
            mPlayButton.setImageDrawable(levelDrawable);

            View controlLayout = (View) ReflectUtils.getObjectField(photoViewer, "videoPlayerControlFrameLayout");
            int height = controlLayout.getHeight();
            ((FrameLayout.LayoutParams) controlLayout.getLayoutParams()).leftMargin = height;
            ((ViewGroup) controlLayout.getParent()).addView(mPlayButton, new FrameLayout.LayoutParams(height, height, Gravity.LEFT | Gravity.BOTTOM));
        } else {
            mPlayButton.setVisibility(View.VISIBLE);
        }
        mPlayButton.setImageLevel(4);
        mPlayButton.setOnClickListener(v -> {
            playPause(videoPlayer);
        });
        mIsPlaying = true;
        mInVideoViewer = true;

        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {
            }

            @Override
            public void onViewDetachedFromWindow(View view) {
                mPlayButton.setVisibility(View.INVISIBLE);
                mPlayButton.setOnClickListener(null);
                mInVideoViewer = false;
                view.setOnTouchListener(null);
                view.removeOnAttachStateChangeListener(this);
            }
        });
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

    private void playPause(Object videoPlayer) {
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
    }

    @Override
    public void android_graphics_drawable_ColorDrawable__draw__Canvas(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (mTransparentPlayDrawable == param.thisObject) {
                if (!mIsPlaying) {
                    mPlayButton.setImageLevel(3);
                    mIsPlaying = true;
                }
            } else if (mTransparentPauseDrawable == param.thisObject) {
                if (mIsPlaying) {
                    mPlayButton.setImageLevel(4);
                    mIsPlaying = false;
                }
            }
        });
    }

    @Override
    public void android_view_View__setVisibility__int(ILTweaks.MethodParam param) {
        param.after(() -> {
            if (mInVideoViewer && mPlayButton != null && param.thisObject instanceof ViewGroup
                    && param.thisObject.getClass().getName().startsWith(PHOTO_VIEWER)) {
                ViewGroup viewGroup = (ViewGroup) param.thisObject;
                if (viewGroup.getChildCount() > 0 && viewGroup.getChildAt(0).getClass().getName().equals(ACTION_BAR_MENU)) {
                    int visibility = (int) param.args[0];
                    mPlayButton.setVisibility(visibility);
                }
            }
        });
    }

    @Override
    public void android_app_Activity__onDestroy__(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (mPlayButton != null && mPlayButton.getContext() == param.thisObject) {
                Logger.d("Destroy mPlayButton");
                mPlayButton = null;
                mTransparentPlayDrawable = null;
                mTransparentPauseDrawable = null;
                mInVideoViewer = false;
                ReflectUtils.setStaticObjectField(findClass(PHOTO_VIEWER), "progressDrawables", null);
            }
        });
    }
}
