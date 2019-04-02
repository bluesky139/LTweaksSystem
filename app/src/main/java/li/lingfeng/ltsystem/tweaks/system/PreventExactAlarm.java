package li.lingfeng.ltsystem.tweaks.system;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.ANDROID, prefs = {})
public class PreventExactAlarm extends PreventRunning {

    private static final long WINDOW_EXACT = 0;
    private static final long WINDOW_HEURISTIC = -1;
    private static final int FLAG_ALLOW_WHILE_IDLE = 1<<2;

    @Override
    protected int getPreventListKey() {
        return R.string.key_prevent_list_prevent_exact_alarm;
    }

    @Override
    public void com_android_server_AlarmManagerService__setImpl__int_long_long_long_PendingIntent_IAlarmListener_String_int_WorkSource_AlarmManager$AlarmClockInfo_int_String(ILTweaks.MethodParam param) {
        param.before(() -> {
            int uid = (int) param.args[10];
            if (sPreventUids.contains(uid)) {
                long windowMillis = (long) param.args[2];
                int flags = (int) param.args[7];
                //Logger.d("Alarm from " + uid + " " + windowMillis + " " + Integer.toBinaryString(flags));

                boolean isSet = false;
                if (windowMillis >= 0) {  // 0 is WINDOW_EXACT, >0 is no later in milliseconds.
                    windowMillis = WINDOW_HEURISTIC;
                    param.setArg(2, windowMillis);
                    isSet = true;
                }
                if ((flags & FLAG_ALLOW_WHILE_IDLE) != 0) {
                    flags &= ~FLAG_ALLOW_WHILE_IDLE;
                    param.setArg(7, flags);
                    isSet = true;
                }

                if (isSet) {
                    Logger.v("Alarm from " + uid + " is set to " + windowMillis + ", " + Integer.toBinaryString(flags));
                }
            }
        });
    }
}
