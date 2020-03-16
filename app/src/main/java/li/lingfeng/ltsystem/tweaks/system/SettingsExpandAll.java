package li.lingfeng.ltsystem.tweaks.system;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.ANDROID_SETTINGS, prefs = R.string.key_app_info_settings_expand_all)
public class SettingsExpandAll extends TweakBase {

    @Override
    public void com_android_settings_widget_HighlightablePreferenceGroupAdapter__adjustInitialExpandedChildCount__SettingsPreferenceFragment(ILTweaks.MethodParam param) {
        param.after(() -> {
            Object host = param.args[0];
            if (host != null) {
                Object screen = ReflectUtils.callMethod(host, "getPreferenceScreen");
                if (screen != null) {
                    Logger.v("setInitialExpandedChildrenCount " + screen + " to max.");
                    ReflectUtils.callMethod(screen, "setInitialExpandedChildrenCount",
                            new Object[] { Integer.MAX_VALUE }, new Class[] { int.class });
                }
            }
        });
    }
}
