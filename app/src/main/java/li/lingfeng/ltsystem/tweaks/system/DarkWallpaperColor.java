package li.lingfeng.ltsystem.tweaks.system;

import android.graphics.Color;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_display_dark_wallpaper_color)
public class DarkWallpaperColor extends TweakBase {

    @Override
    public void android_app_WallpaperColors__WallpaperColors__Color_Color_Color_int(ILTweaks.MethodParam param) {
        param.before(() -> {
            Logger.d("WallpaperColors constructor.");
            Logger.paramArgs(param.args);
            param.setArg(0, Color.valueOf(Color.BLACK));
            param.setArg(1, Color.valueOf(Color.BLACK));
            param.setArg(2, null);
            param.setArg(3, 6);
            Logger.i("WallpaperColors is changed, but keep it black.");
        });
    }
}
