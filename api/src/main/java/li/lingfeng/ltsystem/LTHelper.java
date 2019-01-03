package li.lingfeng.ltsystem;

import android.app.ActivityThread;
import android.app.Application;

public class LTHelper {

    public static Application currentApplication() {
        return ActivityThread.currentApplication();
    }
}
