package li.lingfeng.ltsystem.tweaks.system;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

import java.util.List;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = {}, prefs = {}, excludedPackages = { PackageNames.ANDROID })
public class AppListBlock extends TweakBase {

    @Override
    public void android_app_ApplicationPackageManager__getInstalledApplications__int(ILTweaks.MethodParam param) {
        param.after(() -> {
            List<String> forList = Prefs.large().getStringList(R.string.key_app_list_block_for_list, null);
            if (forList == null || !forList.contains(getPackageName())) {
                return;
            }
            List<String> packageList = Prefs.large().getStringList(R.string.key_app_list_block_package_list, null);
            if (packageList == null || packageList.size() == 0) {
                return;
            }
            List<ApplicationInfo> infos = (List<ApplicationInfo>) param.getResult();
            for (int i = infos.size() - 1; i >= 0; --i) {
                ApplicationInfo info = infos.get(i);
                if (packageList.contains(info.packageName)) {
                    Logger.v("Block package " + info.packageName + " from list for " + getPackageName());
                    infos.remove(i);
                }
            }
        });
    }

    @Override
    public void android_app_ApplicationPackageManager__getInstalledPackages__int(ILTweaks.MethodParam param) {
        param.after(() -> {
            List<String> forList = Prefs.large().getStringList(R.string.key_app_list_block_for_list, null);
            if (forList == null || !forList.contains(getPackageName())) {
                return;
            }
            List<String> packageList = Prefs.large().getStringList(R.string.key_app_list_block_package_list, null);
            if (packageList == null || packageList.size() == 0) {
                return;
            }
            List<PackageInfo> infos = (List<PackageInfo>) param.getResult();
            for (int i = infos.size() - 1; i >= 0; --i) {
                PackageInfo info = infos.get(i);
                if (packageList.contains(info.packageName)) {
                    Logger.v("Block package " + info.packageName + " from list for " + getPackageName());
                    infos.remove(i);
                }
            }
        });
    }
}
