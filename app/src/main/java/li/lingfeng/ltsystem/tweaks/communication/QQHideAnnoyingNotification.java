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

    @Override
    public void android_app_NotificationManager__notify__String_int_Notification(ILTweaks.MethodParam param) {
        param.before(() -> {
            Notification notification = (Notification) param.args[2];
            String title = notification.extras.getString(Notification.EXTRA_TITLE);
            if ("QQ空间动态".equals(title) || "朋友通知".equals(title)) {
                Logger.v("Hide notification " + title);
                param.setResult(null);
            }
        });
    }
}
