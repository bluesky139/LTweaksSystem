package li.lingfeng.ltsystem.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectUtils {

    public static Object callMethod(Object obj, String methodName, Object... args) throws Throwable {
        Method method = obj.getClass().getDeclaredMethod(methodName, Arrays.stream(args).map(Object::getClass).toArray(Class[]::new));
        method.setAccessible(true);
        return method.invoke(obj, args);
    }

    public static Object callMethod(Object obj, String methodName, Object[] args, Class[] parameterTypes) throws Throwable {
        Method method = obj.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(obj, args);
    }

    public static Object callStaticMethod(Class cls, String methodName, Object... args) throws Throwable {
        Method method = cls.getDeclaredMethod(methodName, Arrays.stream(args).map(Object::getClass).toArray(Class[]::new));
        method.setAccessible(true);
        return method.invoke(null, args);
    }

    public static Object getObjectField(Object obj, String fieldName) throws Throwable {
        return findField(obj.getClass(), fieldName).get(obj);
    }

    public static boolean getBooleanField(Object obj, String fieldname) throws Throwable {
        return (boolean) getObjectField(obj, fieldname);
    }

    public static void setObjectField(Object obj, String fieldName, Object value) throws Throwable {
        findField(obj.getClass(), fieldName).set(obj, value);
    }

    public static Field findField(Class cls, String fieldName) throws Throwable {
        Field field = _findField(cls, fieldName);
        field.setAccessible(true);
        return field;
    }

    private static Field _findField(Class cls, String fieldName) throws Throwable {
        try {
            return cls.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            while (true) {
                cls = cls.getSuperclass();
                if (cls == null || cls.equals(Object.class)) {
                    throw e;
                }
                try {
                    return cls.getDeclaredField(fieldName);
                } catch (NoSuchFieldException e1) {
                }
            }
        }
    }
}
