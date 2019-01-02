package li.lingfeng.ltsystem.utils;

import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectUtils {

    public static Object callMethod(Object obj, String methodName, Object... args) throws Throwable {
        Method method = obj.getClass().getDeclaredMethod(methodName, Arrays.stream(args).map(Object::getClass).toArray(Class[]::new));
        method.setAccessible(true);
        return method.invoke(obj, args);
    }

    public static Object callStaticMethod(Class cls, String methodName, Object... args) throws Throwable {
        Method method = cls.getDeclaredMethod(methodName, Arrays.stream(args).map(Object::getClass).toArray(Class[]::new));
        method.setAccessible(true);
        return method.invoke(null, args);
    }
}
