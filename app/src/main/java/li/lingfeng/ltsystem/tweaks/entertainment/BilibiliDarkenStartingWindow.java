package li.lingfeng.ltsystem.tweaks.entertainment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Window;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_bilibili_darken)
public class BilibiliDarkenStartingWindow extends TweakBase {

    @Override
    public void com_android_internal_policy_PhoneWindow__generateLayout__DecorView(ILTweaks.MethodParam param) {
        param.before(() -> {
            Window window = (Window) param.thisObject;
            Context context = window.getContext();
            if (context.getPackageName().equals(PackageNames.BILIBILI)) {
                Drawable drawable = ContextUtils.getColorDrawable("night", context);
                if (drawable != null) {
                    Logger.i("Set night background for bilibili phone window.");
                    window.setBackgroundDrawable(drawable);
                } else {
                    Logger.e("Can't set night backgorund for bilibili phone window.");
                }
            }
        });
    }
}
