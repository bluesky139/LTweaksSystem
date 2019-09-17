package li.lingfeng.ltsystem.tweaks.system;

import android.graphics.drawable.ColorDrawable;
import android.view.Window;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_solid_explorer_darken)
public class SolidExplorerDarkenStartingWindow extends TweakBase {

    @Override
    public void com_android_internal_policy_PhoneWindow__generateLayout__DecorView(ILTweaks.MethodParam param) {
        param.before(() -> {
            Window window = (Window) param.thisObject;
            if (window.getContext().getPackageName().equals(PackageNames.SOLID_EXPLORER)) {
                ColorDrawable drawable = new ColorDrawable(0x303030);
                Logger.i("Set night background for solid explorer phone window.");
                window.setBackgroundDrawable(drawable);
            }
        });
    }
}
