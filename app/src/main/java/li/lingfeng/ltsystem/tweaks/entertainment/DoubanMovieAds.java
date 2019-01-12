package li.lingfeng.ltsystem.tweaks.entertainment;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.DOUBAN_MOVIE, prefs = R.string.key_douban_movie_remove_ads)
public class DoubanMovieAds extends TweakBase {

    @Override
    public void java_util_concurrent_AbstractExecutorService__submit__Runnable(ILTweaks.MethodParam param) {
        param.before(() -> {
            Runnable runnable = (Runnable) param.args[0];
            if (runnable.getClass().getName().startsWith("com.douban.ad.")) {
                Logger.v("Disable douban movie ads request submit.");
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
