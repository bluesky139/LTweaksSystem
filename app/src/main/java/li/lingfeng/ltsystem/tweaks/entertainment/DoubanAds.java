package li.lingfeng.ltsystem.tweaks.entertainment;

import android.content.Intent;

import java.lang.reflect.Field;
import java.util.concurrent.FutureTask;

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
    private static final String TASK_RUNNABLE = "com.mcxiaoke.next.task.TaskRunnable";
    private static final String ITASK_CALLBACKS = "com.mcxiaoke.next.task.ITaskCallbacks";
    private static final String TASK_INFO = "com.mcxiaoke.next.task.TaskInfo";
    private static final String TASK_CALLABLE = "com.mcxiaoke.next.task.TaskCallable";

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
                Logger.v("Block splash ads request.");
                param.setArg(0, (Runnable) () -> {
                    try {
                        Object taskExecutor = ReflectUtils.getSurroundingThis(runnable);
                        String name = (String) ReflectUtils.getObjectField(runnable, "mName");
                        ReflectUtils.callMethod(taskExecutor, "onFinally", name);
                    } catch (Throwable e) {
                        Logger.e("TaskExecutor onFinally exception.", e);
                    }
                });
            } else {
                //Logger.d("runnable " + runnable);
                if (runnable.getClass().getName().equals(TASK_RUNNABLE)) {
                    Field field = ReflectUtils.findFirstFieldByExactType(runnable.getClass(), String.class);
                    String taskTag = (String) field.get(runnable);
                    if (taskTag.startsWith("Object|") || taskTag.startsWith("ReviewFetcher|")) {
                        //Logger.d("taskTag " + taskTag);
                        field = ReflectUtils.findFirstFieldByExactType(runnable.getClass(), findClass(ITASK_CALLBACKS));
                        Object task = field.get(runnable);
                        //Logger.d("task " + task);
                        field = ReflectUtils.findFirstFieldByExactType(task.getClass(), findClass(TASK_INFO));
                        Object taskInfo = field.get(task);
                        //Logger.d("taskInfo " + taskInfo);
                        field = ReflectUtils.findFirstFieldByExactType(taskInfo.getClass(), findClass(TASK_CALLABLE));
                        Object taskCallable = field.get(taskInfo);
                        //Logger.d("taskCallable " + taskCallable);
                        if (taskCallable.getClass().getName().startsWith("com.douban.zeno.Async$")) {
                            field = ReflectUtils.findFirstFieldByExactType(taskCallable.getClass(), findClass("com.douban.zeno.ZenoRequest"));
                            Object request = field.get(taskCallable);
                            //Logger.d("request " + request);
                            if (request.toString().contains("/ad?")) {
                                Logger.v("Block movie review ads request.");
                                param.setResult(new FutureTask<Void>(runnable, null));
                            }
                        }
                    }
                }
            }
        });
    }
}
