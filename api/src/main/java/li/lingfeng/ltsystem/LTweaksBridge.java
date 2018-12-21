package li.lingfeng.ltsystem;

import java.io.File;
import dalvik.system.PathClassLoader;
import android.os.SystemProperties;
import android.util.Log;

public final class LTweaksBridge {

    private static final String TAG = "LTweaksBridge";
    private static final String PACKAGE_NAME = LTweaksBridge.class.getPackage().getName();
    private static final String KEY_APK_PATH = "persist.ltweaks.apk_path";
    private static final String LOADER_CLASS = PACKAGE_NAME + ".Loader";

    public static ILTweaks.Loader loader;

    public static void initInZygote() {
        Log.i(TAG, "Init ltweaks in zygote.");

        try {
            String apkPath = SystemProperties.get(KEY_APK_PATH);
            if (apkPath.isEmpty()) {
                Log.e(TAG, "Apk path not set.");
                return;
            }

            Log.d(TAG, "Apk path " + apkPath);
            File file = new File(apkPath);
            if (!file.exists() || !file.canRead()) {
                Log.e(TAG, "Apk not exist or can't be read.");
                return;
            } 

            ClassLoader classLoader = new PathClassLoader(apkPath, ClassLoader.getSystemClassLoader());
            Class cls = classLoader.loadClass(LOADER_CLASS);
            if (!ILTweaks.Loader.class.isAssignableFrom(cls)) {
                Log.e(TAG, LOADER_CLASS + " is not ILTweaks.");
                return;
            }

            loader = (ILTweaks.Loader) cls.newInstance();
            try {
                loader.initInZygote();
            } catch (Throwable e) {
                Log.e(TAG, "Invoke initInZygote exception.", e);
            }
        } catch (Throwable e) {
            Log.e(TAG, "initInZygote exception.", e);
        }
    }
}