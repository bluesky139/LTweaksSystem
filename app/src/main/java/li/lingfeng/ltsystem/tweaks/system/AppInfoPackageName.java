package li.lingfeng.ltsystem.tweaks.system;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Pair;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.LTHelper;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;

@MethodsLoad(packages = PackageNames.ANDROID_SETTINGS, prefs = R.string.key_app_info_package_name)
public class AppInfoPackageName extends AppInfo {
    @Override
    protected Pair<String, Integer>[] newMenuNames(ILTweaks.MethodParam param) throws Throwable {
        return new Pair[] {
                Pair.create(getPackageName(param), 990)
        };
    }

    @Override
    protected void menuItemSelected(CharSequence menuName, ILTweaks.MethodParam param) throws Throwable {
        ClipboardManager clipboardManager = (ClipboardManager) LTHelper.currentApplication().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText(null, getPackageName(param)));
    }
}
