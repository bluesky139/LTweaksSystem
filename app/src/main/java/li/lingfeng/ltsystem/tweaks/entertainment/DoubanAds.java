package li.lingfeng.ltsystem.tweaks.entertainment;

import android.content.Intent;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.DOUBAN, prefs = R.string.key_douban_remove_ads)
public class DoubanAds extends TweakBase {

    private static final String SPLASH_ACTIVITY = "com.douban.frodo.activity.SplashActivity";
    private static final String AD_PROCESSOR_MANAGER = "com.douban.frodo.util.DoubanAdProcessorManager";

    @Override
    public void android_app_ContextImpl__startActivity__Intent_Bundle(ILTweaks.MethodParam param) {
        param.before(() -> {
            Intent intent = (Intent) param.args[0];
            if (intent.getComponent() != null && intent.getComponent().getClassName().equals(SPLASH_ACTIVITY)) {
                for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                    if (element.getClassName().equals(AD_PROCESSOR_MANAGER)) {
                        Logger.v("Ignore DoubanAdProcessorManager start SplashActivity.");
                        param.setResult(null);
                        return;
                    }
                }
            }
        });
    }

    @Override
    public void java_util_concurrent_AbstractExecutorService__submit__Runnable(ILTweaks.MethodParam param) {
        param.before(() -> {
            Runnable runnable = (Runnable) param.args[0];
            if (runnable.getClass().getName().startsWith("com.douban.ad.")) {
                Logger.v("Disable douban ads request submit.");
                param.setArg(0, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Object taskExecutor = ReflectUtils.getSurroundingThis(runnable);
                            String name = (String) ReflectUtils.getObjectField(runnable, "mName");
                            ReflectUtils.callMethod(taskExecutor, "onFinally", name);
                        } catch (Throwable e) {
                            Logger.e("TaskExecutor onFinally exception.", e);
                        }
                    }
                });
            }
        });
    }
}
