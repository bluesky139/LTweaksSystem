package li.lingfeng.ltsystem.tweaks.google;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_chrome_darken)
public class ChromeDarkenStartingWindow extends TweakBase {

    @Override
    public void com_android_internal_policy_PhoneWindow__generateLayout__DecorView(ILTweaks.MethodParam param) {
        param.before(() -> {
            Window window = (Window) param.thisObject;
            if (window.getContext().getPackageName().equals(PackageNames.CHROME)) {
                ColorDrawable drawable = new ColorDrawable(0x3C3F41);
                Logger.i("Set night background for chrome phone window.");
                window.setBackgroundDrawable(drawable);
                window.setNavigationBarColor(Color.BLACK);
                window.setStatusBarColor(0x313235);
            }
        });
    }
}
