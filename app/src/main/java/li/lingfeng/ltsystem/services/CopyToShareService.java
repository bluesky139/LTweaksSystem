package li.lingfeng.ltsystem.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import org.apache.commons.lang3.StringUtils;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.prefs.NotificationId;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;
import li.lingfeng.ltsystem.utils.ShareUtils;
import li.lingfeng.ltsystem.utils.SimpleSnackbar;

import static li.lingfeng.ltsystem.utils.SimpleSnackbar.FAST_OUT_SLOW_IN_INTERPOLATOR;

public class CopyToShareService extends Service implements ClipboardManager.OnPrimaryClipChangedListener {

    private Handler mHandler;
    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager mWindowManager;
    private SimpleSnackbar mSnackbar;
    private ClipboardManager mClipboardManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i("CopyToShareService onCreate.");
        mHandler = new Handler();
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.gravity = Gravity.BOTTOM;
        mLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mClipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        setupNotification();
        prepareClipboardListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getBooleanExtra("stop", false)) {
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void setupNotification() {
        try {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(getClass().getName());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            String title = getString(R.string.copy_to_share_service);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setWhen(0)
                    .setOngoing(true)
                    .setTicker("Copy to share service")
                    .setDefaults(0)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setContentTitle(title)
                    .setContentText(getString(R.string.copy_to_share_service))
                    .setContentIntent(pendingIntent)
                    .setChannelId(title)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationChannel channel = new NotificationChannel(title, title, NotificationManager.IMPORTANCE_LOW);
            ReflectUtils.setBooleanField(channel, "mBlockableSystem", true);
            notificationManager.createNotificationChannel(channel);
            Notification notification = builder.build();
            startForeground(NotificationId.COPY_TO_SHARE_SERVICE, notification);
        } catch (Throwable e) {
            Logger.e("Can't set notification for " + getClass().getSimpleName(), e);
        }
    }

    private void prepareClipboardListener() {
        mClipboardManager.addPrimaryClipChangedListener(this);
    }

    @Override
    public void onPrimaryClipChanged() {
        try {
            ClipData clipData = mClipboardManager.getPrimaryClip();
            if (clipData == null) {
                return;
            }
            final CharSequence text = clipData.getItemCount() > 0 ? clipData.getItemAt(0).getText() : null;
            if (StringUtils.isEmpty(text)) {
                return;
            }
            Logger.v("Text from clip: " + text);
            dismiss();
            mSnackbar = SimpleSnackbar.make(this, "Got text", SimpleSnackbar.LENGTH_LONG)
                    .setAction(ContextUtils.getLDrawable(R.drawable.ic_search), (v) -> ShareUtils.searchText(this, text.toString()))
                    .setAction(ContextUtils.getLDrawable(R.drawable.ic_incognito), (v) -> ShareUtils.incognitoText(this, text.toString()))
                    .setAction(ContextUtils.getLDrawable(R.drawable.ic_edit), (v) -> ShareUtils.selectText(this, text.toString()))
                    .setAction(ContextUtils.getLDrawable(R.drawable.abc_ic_menu_share_mtrl_alpha), (v) -> ShareUtils.shareText(this, text.toString()));
            mWindowManager.addView(mSnackbar, mLayoutParams);
            mSnackbar.setAlpha(0f);
            mHandler.post(() -> {
                ViewCompat.setTranslationY(mSnackbar, mSnackbar.getHeight());
                ViewCompat.animate(mSnackbar)
                        .translationY(0f)
                        .alpha(1f)
                        .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                        .setDuration(250)
                        .start();
            });
            scheduleDismiss();
        } catch (Throwable e) {
            Logger.e("Share clip with overlay window exception.", e);
        }
    }

    private void scheduleDismiss() {
        mHandler.postDelayed(() -> {
            dismiss();
        }, SimpleSnackbar.LENGTH_LONG);
    }

    private void dismiss() {
        mHandler.removeCallbacksAndMessages(null);
        if (mSnackbar != null && mSnackbar.getParent() != null) {
            ViewCompat.animate(mSnackbar)
                    .translationY(mSnackbar.getHeight())
                    .alpha(0f)
                    .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                    .setDuration(250)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(View view) {
                            mWindowManager.removeView(view);
                        }
                    })
                    .start();
        }
        mSnackbar = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.i("CopyToShareService onDestroy.");
        mClipboardManager.removePrimaryClipChangedListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
