package li.lingfeng.ltsystem.tweaks.communication;

import android.app.Application;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.TELEGRAM, prefs = {})
public class TelegramSeekbarHideDelay extends TweakBase {

    private static final String PHOTO_VIEWER = "org.telegram.ui.PhotoViewer";

    @Override
    public void android_app_Instrumentation__callApplicationOnCreate__Application(ILTweaks.MethodParam param) {
        param.after(() -> {
            Application app = (Application) param.args[0];
            Handler original = (Handler) ReflectUtils.getObjectField(app, "applicationHandler");
            ReflectUtils.setObjectField(app, "applicationHandler", new MyHandler(original));
            Logger.v("Replaced applicationHandler.");
        });
    }

    static class MyHandler extends Handler {

        private Handler mOriginal;

        public MyHandler(Handler original) {
            super();
            mOriginal = original;
        }

        @Override
        public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
            long delay = uptimeMillis - SystemClock.uptimeMillis();
            if (delay <= 3000 && delay > 2900 && msg.getCallback().getClass().getName().startsWith(PHOTO_VIEWER)) {
                int ms = Prefs.instance().getInt(R.string.key_telegram_seekbar_hide_delay, 0) * 1000;
                if (ms > 0) {
                    uptimeMillis = SystemClock.uptimeMillis() + ms;
                    Logger.v("Delay " + ms + "ms for video seekbar hide.");
                }
            }
            return mOriginal.sendMessageAtTime(msg, uptimeMillis);
        }
    }
}
