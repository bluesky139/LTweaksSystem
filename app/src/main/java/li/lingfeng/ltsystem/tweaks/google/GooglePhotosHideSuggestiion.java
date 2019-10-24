package li.lingfeng.ltsystem.tweaks.google;

import android.view.View;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.GOOGLE_PHOTOS, prefs = R.string.key_google_photos_hide_suggestion)
public class GooglePhotosHideSuggestiion extends TweakBase {

    private int mId = -1;

    @Override
    public void android_view_View__setVisibility__int(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (mId == -1) {
                mId = ContextUtils.getIdId("suggested_action_inflated_view");
            }
            if (mId > 0 && ((View) param.thisObject).getId() == mId && (int) param.args[0] == View.VISIBLE) {
                Logger.v("Hide suggested_action_inflated_view.");
                param.setResult(null);
            }
        });
    }
}
