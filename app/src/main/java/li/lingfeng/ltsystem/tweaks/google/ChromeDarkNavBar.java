package li.lingfeng.ltsystem.tweaks.google;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;

@MethodsLoad(packages = PackageNames.CHROME, prefs = R.string.key_chrome_dark_nav_bar)
public class ChromeDarkNavBar extends TweakBase {

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        param.before(() -> {
            Activity activity = (Activity) param.thisObject;
            activity.getWindow().setNavigationBarColor(Color.BLACK);
            View decorView = activity.getWindow().getDecorView();
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        });
    }

    @Override
    public void android_view_View__setSystemUiVisibility__int(ILTweaks.MethodParam param) {
        param.before(() -> {
            param.setArg(0, (int) param.args[0] & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        });
    }

    @Override
    public void com_android_internal_policy_PhoneWindow__setNavigationBarColor__int(ILTweaks.MethodParam param) {
        param.before(() -> {
            if ((int) param.args[0] != Color.BLACK) {
                param.setResult(null);
            }
        });
    }
}
