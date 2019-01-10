package li.lingfeng.ltsystem.tweaks.communication;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_ttrss_darken)
public class TTRssDarkenStartingWindow extends TweakBase {

    @Override
    public void com_android_internal_policy_PhoneWindow__generateLayout__DecorView(ILTweaks.MethodParam param) {
        param.before(() -> {
            Window window = (Window) param.thisObject;
            if (window.getContext().getPackageName().equals(PackageNames.TT_RSS)) {
                ColorDrawable drawable = new ColorDrawable(Color.parseColor("#1c1d1e"));
                Logger.i("Set night background for tt-rss phone window.");
                window.setBackgroundDrawable(drawable);
            }
        });
    }
}
