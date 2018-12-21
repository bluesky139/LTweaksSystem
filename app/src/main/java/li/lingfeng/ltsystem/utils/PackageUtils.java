package li.lingfeng.ltsystem.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;

/**
 * Created by smallville on 2017/1/12.
 */

public class PackageUtils {

    public static final int SORT_BY_NAME = 0;
    public static final int SORT_BY_DATE = 1;

    public static List<PackageInfo> getInstalledPackages() {
        return ILTweaks.currentApplication().getPackageManager().getInstalledPackages(0);
    }

    public static void sortPackages(List<PackageInfo> packages, final int sort) {
        final PackageManager packageManager = ILTweaks.currentApplication().getPackageManager();
        Collections.sort(packages, new Comparator<PackageInfo>() {
            @Override
            public int compare(PackageInfo o1, PackageInfo o2) {
                if (sort == SORT_BY_NAME) {
                    return o1.applicationInfo.loadLabel(packageManager).toString().compareTo(
                            o2.applicationInfo.loadLabel(packageManager).toString());
                } else if (sort == SORT_BY_DATE) {
                    return (int) (o1.firstInstallTime - o2.firstInstallTime);
                } else  {
                    throw new RuntimeException("sortPackages() unknown sort " + sort);
                }
            }
        });
    }

    public static boolean isPackageInstalled(String packageName) {
        try {
            ILTweaks.currentApplication().getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return false;
    }

    public static void uninstallPackage(String packageName) {
        Uri uri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ILTweaks.currentApplication().startActivity(intent);
    }

    public static void tryUninstallPackage(final String packageName, String appName, Activity activity) {
        if (!isPackageInstalled(packageName)) {
            return;
        }
        new AlertDialog.Builder(activity)
                .setMessage(activity.getString(R.string.uninstall_message, appName))
                .setPositiveButton(R.string.uninstall_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        uninstallPackage(packageName);
                    }
                })
                .show();
    }

    /*public static boolean killPackage(Context context, String packageName) throws Throwable {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, 0);
        if (info.uid >= Process.FIRST_APPLICATION_UID && info.uid <= Process.LAST_APPLICATION_UID) {
            Logger.i("Kill " + packageName);
            XposedHelpers.callMethod(activityManager, "forceStopPackage", packageName);

            CharSequence label = info.loadLabel(context.getPackageManager());
            String toastStr = ContextUtils.getLString(R.string.app_kill_hint);
            toastStr = String.format(toastStr, label);
            Toast.makeText(context, toastStr, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Logger.i("Should not kill system app " + packageName);
            return false;
        }
    }*/

    public static int getUid() {
        try {
            return ILTweaks.currentApplication().getPackageManager().getApplicationInfo(
                    ILTweaks.currentApplication().getPackageName(), 0).uid;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }
}
