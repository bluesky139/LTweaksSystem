package li.lingfeng.ltsystem.tweaks.communication;

import android.app.Notification;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.TIM, prefs = R.string.key_qq_hide_annoying_notification)
public class QQHideAnnoyingNotification extends TweakBase {

    private static final long HIDE_IN_MILLISECONDS = 1000;
    private long mNotifyTime = 0;

    @Override
    public void android_app_NotificationManager__notify__String_int_Notification(ILTweaks.MethodParam param) {
        param.before(() -> {
            Notification notification = (Notification) param.args[2];
            String title = notification.extras.getString(Notification.EXTRA_TITLE);
            if ("QQ空间动态".equals(title) || "朋友通知".equals(title)) {
                Logger.v("Hide notification " + title);
                mNotifyTime = System.currentTimeMillis();
                param.setResult(null);
            }
        });
    }

    @Override
    public void android_media_MediaPlayer__start__(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (mNotifyTime > 0 && System.currentTimeMillis() - mNotifyTime < HIDE_IN_MILLISECONDS) {
                param.setResult(null);
            }
        });
    }

    @Override
    public void android_os_SystemVibrator__vibrate__int_String_VibrationEffect_String_AudioAttributes(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (mNotifyTime > 0 && System.currentTimeMillis() - mNotifyTime < HIDE_IN_MILLISECONDS) {
                param.setResult(null);
            }
        });
    }
}
