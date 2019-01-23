package li.lingfeng.ltsystem.tweaks.communication;

import android.media.MediaPlayer;
import android.net.Uri;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.LTHelper;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.TIM, prefs = R.string.key_qq_use_incoming_ringtone)
public class QQIncomingRingtone extends TweakBase {

    private static final String VIDEO_INVITE_FULL = "com.tencent.av.ui.VideoInviteFull";
    private long mCalledTime = 0;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(VIDEO_INVITE_FULL, param, () -> {
            mCalledTime = System.currentTimeMillis();
        });
    }

    @Override
    public void android_media_MediaPlayer__setDataSource__FileDescriptor_long_long(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (System.currentTimeMillis() - mCalledTime < 5000) {
                mCalledTime = 0;
                String path = Prefs.instance().getString(R.string.key_qq_set_incoming_ringtone, "");
                Logger.i("Change media source to " + path);
                MediaPlayer mediaPlayer = (MediaPlayer) param.thisObject;
                mediaPlayer.setDataSource(LTHelper.currentApplication(), Uri.parse(path));
                param.setResult(null);
            }
        });
    }
}
