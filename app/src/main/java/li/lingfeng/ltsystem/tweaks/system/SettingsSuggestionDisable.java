package li.lingfeng.ltsystem.tweaks.system;

import android.content.ComponentName;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.ANDROID_SETTINGS, prefs = R.string.key_app_info_settings_suggestion_disable)
public class SettingsSuggestionDisable extends TweakBase {

    @Override
    public void com_android_settings_dashboard_suggestions_SuggestionFeatureProviderImpl__getSuggestionServiceComponent__(ILTweaks.MethodParam param) {
        param.before(() -> {
            Logger.d("getSuggestionServiceComponent return empty component.");
            param.setResult(new ComponentName("", ""));
        });
    }
}
