package li.lingfeng.ltsystem;

import android.content.Context;

import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;

public class LoadAlways {

    @MethodsLoad(packages = PackageNames.ANDROID, prefs = {})
    public static class Android extends TweakBase {

        @Override
        public void com_android_server_am_ActivityManagerService__ActivityManagerService__Context(ILTweaks.MethodParam param) {
            param.after(() -> {
                LTPrefService.register((Context) param.args[0]);
            });
        }
    }
}
