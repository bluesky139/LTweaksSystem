package li.lingfeng.ltsystem.utils;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import li.lingfeng.ltsystem.LTHelper;

/**
 * Created by smallville on 2016/11/23.
 */
public class Logger {
    private static String TAG = "LTweaks";
    private static Remote remote;

    public static void useRemote(String tag) {
        Logger.d("Use remote log for " + tag);
        TAG += "-" + tag;
        remote = new Remote();
    }

    public static void v(String msg) {
        if (remote == null) {
            Log.v(TAG, msg);
        } else {
            remote.log(TAG, "V", msg);
        }
    }

    public static void d(String msg) {
        if (remote == null) {
            Log.d(TAG, msg);
        } else {
            remote.log(TAG, "D", msg);
        }
    }

    public static void i(String msg) {
        if (remote == null) {
            Log.i(TAG, msg);
        } else {
            remote.log(TAG, "I", msg);
        }
    }

    public static void w(String msg) {
        if (remote == null) {
            Log.w(TAG, msg);
        } else {
            remote.log(TAG, "W", msg);
        }
    }

    public static void e(String msg) {
        if (remote == null) {
            Log.e(TAG, msg);
        } else {
            remote.log(TAG, "E", msg);
        }
    }

    public static void e(String msg, Throwable e) {
        if (remote == null) {
            Log.e(TAG, msg, e);
        } else {
            remote.log(TAG, "E", msg + "\n" + Log.getStackTraceString(e));
        }
    }

    public static void stackTrace() {
        stackTrace("");
    }

    public static void stackTrace(String message) {
        if (remote == null) {
            Log.v(TAG, "[print stack] " + message);
        } else {
            remote.log(TAG, "V", "[print stack] " + message);
        }
        stackTrace(new Exception("[print stack] " + message));
    }

    public static void stackTrace(Throwable e) {
        if (e instanceof InvocationTargetException) {
            e = ((InvocationTargetException) e).getTargetException();
        }
        if (remote == null) {
            Log.e(TAG, Log.getStackTraceString(e));
        } else {
            remote.log(TAG, "E", Log.getStackTraceString(e));
        }
    }

    public static void intent(Intent intent) {
        if (intent == null) {
            Logger.d(" intent is null.");
            return;
        }
        Logger.d(" intent action: " + intent.getAction());
        Logger.d(" intent component: " + (intent.getComponent() != null ? intent.getComponent().toShortString() : ""));
        Logger.d(" intent type: " + intent.getType());
        Logger.d(" intent flag: 0x" + Integer.toHexString(intent.getFlags()));
        Logger.d(" intent data: " + intent.getData());
        if (intent.getExtras() != null) {
            for (String key : intent.getExtras().keySet()) {
                Logger.d(" intent extra: " + key + " -> " + intent.getExtras().get(key));
            }
        }
    }

    public static void paramArgs(Object[] args) {
        for (Object arg : args) {
            Logger.d(" param arg: " + arg);
        }
    }

    public static void map(Map map) {
        for (Object _kv : map.entrySet()) {
            Map.Entry kv = (Map.Entry) _kv;
            Logger.d(" map "  + kv.getKey() + ": " + kv.getValue());
        }
    }

    public static void clsFieldValues(Object instance) throws Throwable {
        Logger.d("clsFieldValues " + instance);
        List<Field> fields = FieldUtils.getAllFieldsList(instance.getClass());
        for (Field field : fields) {
            field.setAccessible(true);
            Logger.d("  " + field.getName() + ": " + field.get(instance));
        }
    }

    static class Remote {
        void log(String tag, String level, String msg) {
            ContentValues values = new ContentValues();
            values.put("tag", tag);
            values.put("level", level);
            values.put("msg", msg);
            LTHelper.currentApplication().getContentResolver()
                    .insert(Uri.parse("content://li.lingfeng.ltsystem.remoteLog/"), values);
        }
    }
}
