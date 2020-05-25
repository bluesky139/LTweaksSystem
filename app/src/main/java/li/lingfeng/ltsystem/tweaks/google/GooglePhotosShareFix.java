package li.lingfeng.ltsystem.tweaks.google;

import android.content.Intent;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;

@MethodsLoad(packages = PackageNames.GOOGLE_PHOTOS, prefs = R.string.key_google_photos_share_fix)
public class GooglePhotosShareFix extends TweakBase {

    @Override
    public void android_app_Activity__startActivityForResult__Intent_int_Bundle(ILTweaks.MethodParam param) {
        param.before(() -> {
            Intent intent = (Intent) param.args[0];
            if (Intent.ACTION_SEND.equals(intent.getAction()) || Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            }
        });
    }
}
