package li.lingfeng.ltsystem.tweaks.communication;

import android.app.Activity;
import android.content.Intent;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.PUSH_BULLET, prefs = R.string.key_push_bullet_share_to_all)
public class PushBulletShareToAll extends TweakBase {

    private static final String SHARE_ACTIVITY = "com.pushbullet.android.ui.ShareActivity";

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        beforeOnClass(SHARE_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            Intent intent = activity.getIntent();
            if (!intent.hasExtra("stream_key")) {
                Logger.i("Share to all devices.");
                intent.putExtra("stream_key", "all-of-my-devices");
            }
        });
    }
}
