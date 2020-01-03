package li.lingfeng.ltsystem.tweaks.system;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.RemoteException;
import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import li.lingfeng.ltsystem.ILTPrefListener;
import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.LTHelper;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.ANDROID, prefs = {})
public class PreventProcess extends TweakBase {

    private static final int DELAY_SECONDS = 120;

    private Object mActivityManagerService;
    private Handler mHandler;
    private boolean mLogIfTransactionTooLarge = false;

    private List<String> mPreventList;
    private Map<String, SparseIntArray> mActivityCounter;
    private Map<String, Runnable> mStopRunnables = new HashMap<>();

    @Override
    public void com_android_server_am_ActivityManagerService__finishBooting__(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (mPreventList != null) {
                return;
            }
            List<String> packageList = Prefs.large().getStringList(R.string.key_prevent_process_list, new ArrayList<>(), false);
            mPreventList = new ArrayList<>(packageList.size());
            mActivityCounter = new HashMap<>(packageList.size());
            for (String packageName : packageList) {
                Logger.v("PreventProcess add package " + packageName);
                mPreventList.add(packageName);
                mActivityCounter.put(packageName, new SparseIntArray());
            }

            Prefs.large().addListener(R.string.key_prevent_process_list, new ILTPrefListener.Stub() {
                @Override
                public void onPrefChanged(String key) throws RemoteException {
                    List<String> newPackageList = Prefs.large().getStringList(R.string.key_prevent_process_list, null, false);
                    if (newPackageList != null && newPackageList.size() > 0) {
                        Iterator<String> it = mPreventList.iterator();
                        while (it.hasNext()) {
                            String packageName = it.next();
                            if (!newPackageList.contains(packageName)) {
                                Logger.v("PreventProcess remove package " + packageName);
                                it.remove();
                                mActivityCounter.remove(packageName);
                            }
                        }
                        for (String packageName : newPackageList) {
                            if (!mPreventList.contains(packageName)) {
                                Logger.v("PreventProcess add package " + packageName);
                                mPreventList.add(packageName);
                                mActivityCounter.put(packageName, new SparseIntArray());
                                forceStopPackage(packageName);
                            }
                        }
                    } else {
                        Logger.v("PreventProcess clear packages.");
                        mPreventList.clear();
                        mActivityCounter.clear();
                    }
                }
            });

            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
            filter.addDataScheme("package");
            LTHelper.currentApplication().registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String packageName = intent.getData().getSchemeSpecificPart();
                    Logger.v("PreventProcess receive " + intent.getAction() + " on " + packageName);
                    if (mPreventList.contains(packageName)) {
                        Logger.v("PreventProcess remove package " + packageName);
                        mPreventList.remove(packageName);
                        mActivityCounter.remove(packageName);
                        Prefs.large().putStringList(R.string.key_prevent_process_list, mPreventList);
                    }
                }
            }, filter);
        });
    }

    private void forceStopPackage(final String packageName) {
        try {
            Logger.v("forceStopPackage " + packageName);
            ReflectUtils.callMethod(mActivityManagerService, "forceStopPackage",
                    new Object[] { packageName, 0 }, new Class[] { String.class, int.class });
        } catch (Throwable e) {
            Logger.e("forceStopPackage exception.", e);
        }
    }

    private void scheduleForceStopPackage(final String packageName) {
        if (mStopRunnables.get(packageName) != null) {
            Logger.e("Already scheduled stop package " + packageName);
            return;
        }
        Logger.v("scheduleForceStopPackage " + packageName);
        Runnable runnable = () -> {
            mStopRunnables.remove(packageName);
            forceStopPackage(packageName);
        };
        mStopRunnables.put(packageName, runnable);
        mHandler.postDelayed(runnable, DELAY_SECONDS * 1000);
    }

    private void cancelForceStopPackage(String packageName) {
        Logger.v("cancelForceStopPackage " + packageName);
        Runnable runnable = mStopRunnables.get(packageName);
        if (runnable != null) {
            mStopRunnables.remove(packageName);
            mHandler.removeCallbacks(runnable);
        }
    }

    @Override
    public void com_android_server_am_ActivityManagerService__ActivityManagerService__Context(ILTweaks.MethodParam param) {
        param.after(() -> {
            Logger.i("Init PreventProcess.");
            mActivityManagerService = param.thisObject;
            mHandler = new Handler();
        });
    }

    @Override
    public void com_android_server_am_ActivityManagerService__startProcessLocked__String_ApplicationInfo_boolean_int_String_ComponentName_boolean_boolean_int_boolean_String_String_String$array_Runnable(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (mPreventList == null) {
                return;
            }
            ApplicationInfo info = (ApplicationInfo) param.args[1];
            if (mPreventList.contains(info.packageName)) {
                String processName = (String) param.args[0];
                String hostingType = (String) param.args[4];
                ComponentName componentName = (ComponentName) param.args[5];
                Logger.d("startProcessLocked " + processName + " " + hostingType + " " + componentName);

                if (mActivityCounter.get(info.packageName).size() == 0 && mStopRunnables.get(info.packageName) == null) {
                    if ("service".equals(hostingType) || "broadcast".equals(hostingType) || "content provider".equals(hostingType)) {
                        Logger.v("Prevent " + hostingType + " " + componentName);
                        param.setResult(null);
                    } else if (!hostingType.equals("activity")) {
                        Logger.w("Pass " + hostingType + " " + componentName);
                    }
                }
            }
        });
    }

    @Override
    public void com_android_server_am_ActivityManagerService__handleAppDiedLocked__ProcessRecord_boolean_boolean(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (mPreventList == null) {
                return;
            }
            Object processRecord = param.args[0];
            String processName = (String) ReflectUtils.getObjectField(processRecord, "processName");
            if (mPreventList.contains(processName)) {
                Logger.d("handleAppDiedLocked " + processRecord);
                ApplicationInfo info = (ApplicationInfo) ReflectUtils.getObjectField(processRecord, "info");
                SparseIntArray counters = mActivityCounter.get(info.packageName);
                int pid = ReflectUtils.getIntField(processRecord, "pid");
                int count = counters.get(pid, 0);
                if (count > 0) {
                    Logger.w("process " + info.packageName + "/" + pid + " died with " + count + " activity count.");
                    counters.delete(pid);
                    if (counters.size() > 0) {
                        Logger.d("left " + counters.size() + " " + info.packageName + " counters.");
                    } else {
                        scheduleForceStopPackage(info.packageName);
                    }
                }
            }
        });
    }

    @Override
    public void com_android_server_am_ActivityStack__cleanUpActivityLocked__ActivityRecord_boolean_boolean(ILTweaks.MethodParam param) {
        param.after(() -> {
            if (mPreventList == null) {
                return;
            }
            Object activityRecord = param.args[0];
            if (!((boolean) param.args[1]) && !((boolean) param.args[2])) {
                if (ReflectUtils.getObjectField(param.args[0], "app") != null) {
                    ActivityInfo info = (ActivityInfo) ReflectUtils.getObjectField(activityRecord, "info");
                    if (mPreventList.contains(info.packageName)) {
                        Logger.d("cleanUpActivityLocked " + activityRecord);
                        SparseIntArray counters = mActivityCounter.get(info.packageName);
                        Object processRecord = ReflectUtils.getObjectField(activityRecord, "app");
                        int pid = ReflectUtils.getIntField(processRecord, "pid");
                        int count = counters.get(pid) - 1;
                        Logger.d("decrease " + info.packageName + "/" + pid + " " + count);
                        if (count > 0) {
                            counters.put(pid, count);
                        } else {
                            counters.delete(pid);
                            if (counters.size() > 0) {
                                Logger.d("left " + counters.size() + " " + info.packageName + " counters.");
                            } else {
                                scheduleForceStopPackage(info.packageName);
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void com_android_server_am_ActivityStackSupervisor__realStartActivityLocked__ActivityRecord_ProcessRecord_boolean_boolean(ILTweaks.MethodParam param) {
        param.before(() -> {
            mLogIfTransactionTooLarge = false;
        });
        param.after(() -> {
            if (mLogIfTransactionTooLarge) {
                mLogIfTransactionTooLarge = false;
                if (mPreventList == null) {
                    return;
                }
                Object activityRecord = param.args[0];
                ActivityInfo info = (ActivityInfo) ReflectUtils.getObjectField(activityRecord, "info");
                if (mPreventList.contains(info.packageName)) {
                    Logger.d("realStartActivityLocked " + activityRecord);
                    SparseIntArray counters = mActivityCounter.get(info.packageName);
                    Object processRecord = ReflectUtils.getObjectField(activityRecord, "app");
                    int pid = ReflectUtils.getIntField(processRecord, "pid");
                    int count = counters.get(pid, 0) + 1;
                    Logger.d("increase " + info.packageName + "/" + pid + " " + count);
                    counters.put(pid, count);
                    cancelForceStopPackage(info.packageName);
                }
            }
        });
    }

    @Override
    public void com_android_server_am_ActivityStackSupervisor__logIfTransactionTooLarge__Intent_Bundle(ILTweaks.MethodParam param) {
        param.before(() -> {
            mLogIfTransactionTooLarge = true;
        });
    }
}
