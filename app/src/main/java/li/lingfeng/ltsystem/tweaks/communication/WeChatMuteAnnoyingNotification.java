package li.lingfeng.ltsystem.tweaks.communication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.StatusBarNotification;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.LTHelper;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_wechat_mute_annoying_notification)
public class WeChatMuteAnnoyingNotification extends TweakBase {

    private static final int MAX_NOTIFY_COUNT = 3;
    private static final int MIN_NOTIFY_SECONDS = 5;
    private int mNotifyCount = -1;
    private long mLastNotifyTime = 0;

    @Override
    public void com_android_server_am_ActivityManagerService__finishBooting__(ILTweaks.MethodParam param) {
        param.after(() -> {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            LTHelper.currentApplication().registerReceiver(mReceiver, filter);
        });
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                if (mNotifyCount == MAX_NOTIFY_COUNT) {
                    Logger.i("Unmute WeChat by count.");
                }
                mNotifyCount = -1;
            } else {
                mNotifyCount = 0;
            }
        }
    };

    @Override
    public void com_android_server_notification_NotificationManagerService__shouldMuteNotificationLocked__NotificationRecord(ILTweaks.MethodParam param) {
        param.before(() -> {
            StatusBarNotification sbn = (StatusBarNotification) ReflectUtils.getObjectField(param.args[0], "sbn");
            if (sbn.getPackageName().equals(PackageNames.WE_CHAT)) {
                if (mNotifyCount >= 0 && mNotifyCount != MAX_NOTIFY_COUNT) {
                    ++mNotifyCount;
                    Logger.d("WeChat mNotifyCount " + mNotifyCount);
                    if (mNotifyCount == MAX_NOTIFY_COUNT) {
                        Logger.i("Mute WeChat by count.");
                    }
                }
                if (mNotifyCount == MAX_NOTIFY_COUNT) {
                    param.setResult(true);
                } else if (mNotifyCount == -1) {
                    if (System.currentTimeMillis() - mLastNotifyTime < MIN_NOTIFY_SECONDS * 1000) {
                        param.setResult(true);
                    } else {
                        mLastNotifyTime = System.currentTimeMillis();
                    }
                }
            }
        });
    }
}
