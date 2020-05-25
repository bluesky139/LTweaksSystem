package li.lingfeng.ltsystem.utils;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    public static void w(String msg, Throwable e) {
        if (remote == null) {
            Log.w(TAG, msg, e);
        } else {
            remote.log(TAG, "W", msg + "\n" + Log.getStackTraceString(e));
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

    public static void throwException(Throwable e) {
        e("throwException", e);
        throw new RuntimeException(e);
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
        intent(intent, 1);
    }

    public static void intent(Intent intent, int deep) {
        String space = StringUtils.repeat(' ', deep);
        if (intent == null) {
            Logger.d(space + "intent is null.");
            return;
        }
        Logger.d(space + "intent action: " + intent.getAction());
        Logger.d(space + "intent package: " + intent.getPackage());
        Logger.d(space + "intent component: " + (intent.getComponent() != null ? intent.getComponent().toShortString() : ""));
        Logger.d(space + "intent type: " + intent.getType());
        Logger.d(space + "intent flag: 0x" + Integer.toHexString(intent.getFlags()));
        Logger.d(space + "intent data: " + intent.getData());
        if (intent.getExtras() != null) {
            for (String key : intent.getExtras().keySet()) {
                Object value = intent.getExtras().get(key);
                Logger.d(space + "intent extra: " + key + " -> " + value
                        + " (" + (value != null ? value.getClass().getName() : "") + ")");
                if (value != null && value.getClass() == Intent.class) {
                    Logger.intent((Intent) value, deep + 1);
                }
            }
        }
    }

    public static void bundle(Bundle bundle) {
        if (bundle == null) {
            Logger.d(" bundle is null.");
            return;
        }
        for (String key : bundle.keySet()) {
            Logger.d(" bundle: " + key + " -> " + bundle.get(key));
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

    public static void clsMethods(Object instance) throws Throwable {
        Logger.d("clsMethods " + instance);
        Class cls = instance.getClass();
        List<Class<?>> classes = (List<Class<?>>) ReflectUtils.callStaticMethod(MethodUtils.class, "getAllSuperclassesAndInterfaces", cls);
        classes.add(0, cls);
        for (Class<?> acls : classes) {
            final Method[] methods = acls.getDeclaredMethods();
            for (final Method method : methods) {
                Logger.d(" " + method);
            }
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
