package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.preference.PreferenceManager;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.BILIBILI, prefs = R.string.key_bilibili_danmaku_off)
public class BilibiliDanmakuOff extends TweakBase {

    private static final String VIDEO_DETAILS_ACTIVITY = "tv.danmaku.bili.ui.video.VideoDetailsActivity";

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        beforeOnClass(VIDEO_DETAILS_ACTIVITY, param, () -> {
            Logger.v("Set danmaku off by default.");
            Activity activity = (Activity) param.thisObject;
            PreferenceManager.getDefaultSharedPreferences(activity).edit()
                    .putBoolean("danmaku_switch", false)
                    .commit();
        });
    }

    @Override
    public void android_app_SharedPreferencesImpl$EditorImpl__putBoolean__String_boolean(ILTweaks.MethodParam param) {
        param.before(() -> {
            if ("danmaku_switch".equals(param.args[0]) && (boolean) param.args[1]) {
                Logger.d("Put danmaku_switch false.");
                param.setArg(1, false);
            }
        });
    }
}
