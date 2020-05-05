package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.view.View;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.BILIBILI, prefs = R.string.key_bilibili_hide_follow)
public class BilibiliHideFollow extends TweakBase {

    private static final String AUTHOR_SPACE_ACTIVITY = "com.bilibili.app.authorspace.ui.AuthorSpaceActivity";

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(AUTHOR_SPACE_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            View followGuideView = ViewUtils.findViewByName(activity, "follow_guide");
            if (followGuideView != null) {
                Logger.v("Remove follow_guide.");
                ViewUtils.removeView(followGuideView);
            }
        });
    }
}
