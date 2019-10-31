package li.lingfeng.ltsystem.tweaks.google;

import android.app.Activity;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.GMS, prefs = R.string.key_google_play_services_dismiss_system_update)
public class GooglePlayServicesDismissSystemUpdate extends TweakBase {

    private static final String UPDATE_DIALOG = "com.google.android.gms.update.phone.PopupDialog";

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(UPDATE_DIALOG, param, () -> {
            Logger.v("Dismiss system update dialog.");
            Activity activity = (Activity) param.thisObject;
            activity.finish();
        });
    }
}
