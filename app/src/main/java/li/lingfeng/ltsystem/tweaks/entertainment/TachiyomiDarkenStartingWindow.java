package li.lingfeng.ltsystem.tweaks.entertainment;

import android.graphics.drawable.ColorDrawable;
import android.view.Window;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_tachiyomi_darken)
public class TachiyomiDarkenStartingWindow extends TweakBase {

    @Override
    public void com_android_internal_policy_PhoneWindow__generateLayout__DecorView(ILTweaks.MethodParam param) {
        param.before(() -> {
            Window window = (Window) param.thisObject;
            if (window.getContext().getPackageName().equals(PackageNames.TACHIYOMI)) {
                ColorDrawable drawable = new ColorDrawable(0x1C1C1D);
                Logger.i("Set night background for Tachiyomi phone window.");
                window.setBackgroundDrawable(drawable);
                window.setNavigationBarColor(0x1C1C1D);
            }
        });
    }
}
