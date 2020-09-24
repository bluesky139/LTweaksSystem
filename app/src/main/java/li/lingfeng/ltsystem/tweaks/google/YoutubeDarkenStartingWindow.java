package li.lingfeng.ltsystem.tweaks.google;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_youtube_darken)
public class YoutubeDarkenStartingWindow extends TweakBase {

    @Override
    public void com_android_internal_policy_PhoneWindow__generateLayout__DecorView(ILTweaks.MethodParam param) {
        param.after(() -> {
            Window window = (Window) param.thisObject;
            Context context = window.getContext();
            if (context.getPackageName().equals(PackageNames.YOUTUBE)) {
                View decorView = (View) param.args[0];
                if (decorView.getBackground() instanceof ColorDrawable) {
                    Logger.i("Set night background for Youtube phone window.");
                    Drawable drawable = new ColorDrawable(0xFF282828);
                    window.setBackgroundDrawable(drawable);
                    window.setNavigationBarColor(Color.BLACK);
                    window.setStatusBarColor(Color.BLACK);
                }
            }
        });
    }
}
