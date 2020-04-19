package li.lingfeng.ltsystem.tweaks.communication;

import android.content.Context;
import android.net.Uri;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.WE_CHAT, prefs = R.string.key_wechat_use_incoming_ringtone)
public class WeChatIncomingRingtone extends TweakBase {

    @Override
    public void android_media_MediaPlayer__setDataSource__Context_Uri(ILTweaks.MethodParam param) {
        param.before(() -> {
            Context context = (Context) param.args[0];
            Uri uri = (Uri) param.args[1];
            Logger.i("Setting media source, original is " + uri.toString());

            int idPhonering = context.getResources().getIdentifier("phonering", "raw", "com.tencent.mm");
            if (uri.toString().equals("android.resource://com.tencent.mm/" + idPhonering)) {
                String path = Prefs.instance().getString(R.string.key_wechat_set_incoming_ringtone, "");
                param.setArg(1, Uri.parse(path));
                Logger.i("Media source is changed to " + path);
            }
        });
    }
}
