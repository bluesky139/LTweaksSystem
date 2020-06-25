package li.lingfeng.ltsystem.tweaks.system;

import android.content.Intent;
import android.content.pm.PackageParser;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_text_normalize_translate_menu)
public class NormalizeTranslateFloatingMenu extends TweakBase {

    @Override
    public void android_content_pm_PackageParser__parsePackage__File_int_boolean(ILTweaks.MethodParam param) {
        param.after(() -> {
            PackageParser.Package pkg = (PackageParser.Package) param.getResult();
            if (pkg == null) {
                return;
            }
            for (PackageParser.Activity activity : pkg.activities) {
                for (PackageParser.IntentInfo intent : activity.intents) {
                    if (intent.countActions() > 0 && intent.getAction(0).equals(Intent.ACTION_TRANSLATE)) {
                        Logger.i("Remove android.intent.action.TRANSLATE from " + activity);
                        activity.intents.remove(intent);
                        return;
                    }
                }
            }
        });
    }
}
