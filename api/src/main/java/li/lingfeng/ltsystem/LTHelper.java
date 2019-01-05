package li.lingfeng.ltsystem;

import android.app.ActivityThread;
import android.app.Application;

/**
 * {@hide}
 */
public class LTHelper {

    public static Application currentApplication() {
        return ActivityThread.currentApplication();
    }
}
