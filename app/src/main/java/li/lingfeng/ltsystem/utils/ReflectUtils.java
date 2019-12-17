package li.lingfeng.ltsystem.utils;

import android.text.DynamicLayout;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;

import li.lingfeng.ltsystem.LTHelper;

public class ReflectUtils {

    private static HashMap<String, Field> fieldCache = new HashMap<>();
    private static HashMap<String, Method> methodCache = new HashMap<>();

    public static Object callMethod(Object obj, String methodName, Object... args) throws Throwable {
        Method method = findMethod(obj.getClass(), methodName, Arrays.stream(args).map(Object::getClass).toArray(Class[]::new));
        method.setAccessible(true);
        return method.invoke(obj, args);
    }

    public static Object callMethod(Object obj, String methodName, Object[] args, Class[] parameterTypes) throws Throwable {
        Method method = findMethod(obj.getClass(), methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(obj, args);
    }

    public static Object callStaticMethod(Class cls, String methodName, Object... args) throws Throwable {
        Method method = findMethod(cls, methodName, Arrays.stream(args).map(Object::getClass).toArray(Class[]::new));
        method.setAccessible(true);
        return method.invoke(null, args);
    }

    public static Method findMethod(Class cls, String methodName, Class[] parameterTypes) throws Throwable {
        StringBuilder builder = new StringBuilder();
        builder.append(cls.getName());
        builder.append('#');
        builder.append(methodName);
        builder.append('(');
        for (int i = 0; i < parameterTypes.length; ++i) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(parameterTypes[i].getName());
        }
        builder.append(')');
        String fullMethodName = builder.toString();
        Method method = methodCache.get(fullMethodName);
        if (method == null) {
            method = _findMethod(cls, methodName, parameterTypes);
            method.setAccessible(true);
            methodCache.put(fullMethodName, method);
        }
        return method;
    }

    private static Method _findMethod(Class cls, String methodName, Class[] parameterTypes) throws Throwable {
        try {
            return cls.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            while (true) {
                cls = cls.getSuperclass();
                if (cls == null || cls.equals(Object.class)) {
                    throw e;
                }
                try {
                    return cls.getDeclaredMethod(methodName, parameterTypes);
                } catch (NoSuchMethodException e1) {
                }
            }
        }
    }

    public static Object getObjectField(Object obj, String fieldName) throws Throwable {
        return findField(obj.getClass(), fieldName).get(obj);
    }

    public static Object getStaticObjectField(Class cls, String fieldName) throws Throwable {
        return findField(cls, fieldName).get(null);
    }

    public static boolean getBooleanField(Object obj, String fieldName) throws Throwable {
        return findField(obj.getClass(), fieldName).getBoolean(obj);
    }

    public static int getIntField(Object obj, String fieldName) throws Throwable {
        return findField(obj.getClass(), fieldName).getInt(obj);
    }

    public static float getFloatField(Object obj, String fieldName) throws Throwable {
        return findField(obj.getClass(), fieldName).getFloat(obj);
    }

    public static void setObjectField(Object obj, String fieldName, Object value) throws Throwable {
        findField(obj.getClass(), fieldName).set(obj, value);
    }

    public static void setStaticObjectField(Class cls, String fieldName, Object value) throws Throwable {
        findField(cls, fieldName).set(null, value);
    }

    public static void setIntField(Object obj, String fieldName, int value) throws Throwable {
        findField(obj.getClass(), fieldName).setInt(obj, value);
    }

    public static void setBooleanField(Object obj, String fieldName, boolean value) throws Throwable {
        findField(obj.getClass(), fieldName).setBoolean(obj, value);
    }

    public static Field findField(Class cls, String fieldName) throws Throwable {
        StringBuilder builder = new StringBuilder();
        builder.append(cls.getName());
        builder.append('#');
        builder.append(fieldName);
        String fullFieldName = builder.toString();
        Field field = fieldCache.get(fullFieldName);
        if (field == null) {
            field = _findField(cls, fieldName);
            field.setAccessible(true);
            fieldCache.put(fullFieldName, field);
        }
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

    public static Field findFirstFieldByExactType(Class cls, Class type) throws Throwable {
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType() == type) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new NoSuchFieldError("findFirstFieldByExactType " + type + " in " + cls);
    }

    public static void printFields(Object obj) throws Throwable {
        Field[] fields = obj.getClass().getFields();
        for (Field field : fields) {
            if (Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            Object value = field.get(obj);
            Logger.v(" field[" + field.getName() + "] " + value
                    + (value == null ? "" :
                     (field.getType() == DynamicLayout.class ? " " + getObjectField(value, "mBase") : ""))
                    );
        }
    }

    public static Object getSurroundingThis(Object obj) throws Throwable {
        String name = obj.getClass().getName();
        int pos = name.lastIndexOf('$');
        if (pos > 0) {
            name = name.substring(0, pos);
            Class cls = findClass(name, obj.getClass().getClassLoader());
            Field field = findFirstFieldByExactType(obj.getClass(), cls);
            return field.get(obj);
        }
        return null;
    }

    public static Constructor getFirstConstructor(Class cls) {
        Constructor constructor = cls.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        return constructor;
    }

    public static Class findClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        return Class.forName(className, false, classLoader);
    }
}
