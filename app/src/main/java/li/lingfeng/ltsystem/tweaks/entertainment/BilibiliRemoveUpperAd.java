package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.view.View;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.BILIBILI, prefs = R.string.key_bilibili_remove_upper_ad)
public class BilibiliRemoveUpperAd extends TweakBase {

    private static final String VIDEO_DETAILS_ACTIVITY = "com.bilibili.video.videodetail.VideoDetailsActivity";

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(VIDEO_DETAILS_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            View adContainer = ViewUtils.findViewByName(activity, "upper_ad_container");
            adContainer.setVisibility(View.GONE);
        });
    }
}
