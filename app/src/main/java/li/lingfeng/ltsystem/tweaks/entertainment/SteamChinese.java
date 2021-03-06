package li.lingfeng.ltsystem.tweaks.entertainment;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.STEAM, prefs = R.string.key_steam_chinese)
public class SteamChinese extends TweakBase {

    // https://github.com/Flo354/XposedAppLocale/blob/master/app/src/main/java/com/flo354/xposed/applocale/XposedMod.java
    @Override
    public void android_content_ContextWrapper__attachBaseContext__Context(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (param.args[0] == null || !(param.args[0] instanceof Context)) {
                Logger.stackTrace("null ??? " + param.args[0]);
                return;
            }
            Logger.i("Set zh-CN locale.");
            Context context = (Context) param.args[0];
            Configuration config = new Configuration(context.getResources().getConfiguration());
            config.setLocale(new Locale("zh", "CN"));
            context = context.createConfigurationContext(config);
            param.setArg(0, context);
        });
    }
}
