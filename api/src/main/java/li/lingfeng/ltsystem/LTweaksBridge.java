package li.lingfeng.ltsystem;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dalvik.system.PathClassLoader;

public final class LTweaksBridge extends ILTweaksBridge {

    private static final String TAG = "LTweaksBridge";
    private static final String PACKAGE_NAME = LTweaksBridge.class.getPackage().getName();
    private static final String LOADER_CLASS = PACKAGE_NAME + ".Loader";

    public static void initInZygote() {
        Log.i(TAG, "Init ltweaks in zygote.");

        try {
            File file = new File("/data/system/packages.xml");
            if (!file.exists() || !file.canRead()) {
                Log.e(TAG, "Can't read packages.xml");
                return;
            }

            // <package name="li\.lingfeng\.ltsystem" codePath="([^"]+)"
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            Pattern pattern = Pattern.compile("<package name=\"li\\.lingfeng\\.ltsystem\" codePath=\"([^\"]+)\" ");
            String line;
            String apkPath = null;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    apkPath = matcher.group(1) + "/base.apk";
                    break;
                }
            }
            reader.close();
            if (apkPath == null) {
                Log.e(TAG, "Apk path is null.");
                return;
            }

            Log.d(TAG, "Apk path " + apkPath);
            file = new File(apkPath);
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

            paramCreator = new LTweaksImpl.ParamCreatorImpl();
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