package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.Shell;

@MethodsLoad(packages = PackageNames.MX_PLAYER_PRO, prefs = R.string.key_mxplayer_external_storage)
public class MXPlayerExternalStorage extends TweakBase {

    private static final String MEDIA_LIST_ACTIVITY = "com.mxtech.videoplayer.pro.ActivityMediaList";
    private File mSdcardDir;

    @Override
    public void android_os_Environment__getExternalStorageDirectory__(ILTweaks.MethodParam param) {
        param.before(() -> {
            param.setResult(getSdcardDir());
            Logger.v("getExternalStorageDirectory " + getSdcardDir());
        });
    }

    private File getSdcardDir() {
        if (mSdcardDir == null) {
            mSdcardDir = new File("/storage/emulated/0/Movies/mxplayer_rclone");
            if (!mSdcardDir.exists()) {
                mSdcardDir.mkdirs();
            }
            try {
                new File(mSdcardDir, ".nomedia").createNewFile();
            } catch (IOException e) {
            }
        }
        return mSdcardDir;
    }

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        beforeOnClass(MEDIA_LIST_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            File dir = new File(getSdcardDir(), "cloud0");
            if (!dir.exists()) {
                dir.mkdir();
            }
            File cacheDir = new File(getSdcardDir(), "rclone_cache");
            if (!cacheDir.exists()) {
                cacheDir.createNewFile();
            }
            File logFile = new File(getSdcardDir(), "rclone.log");
            if (logFile.exists()) {
                logFile.delete();
            }

            String cmd = "/sbin/rclone mount cloud0: " + dir.getPath() +
                    " --config /sdcard/rclone_mxplayer.conf" +
                    " --max-read-ahead 128k" +
                    " --buffer-size 32M" +
                    " --dir-cache-time 1h" +
                    " --poll-interval 5m" +
                    " --attr-timeout 1h" +
                    " --vfs-cache-mode full" +
                    " --vfs-read-ahead 8M" +
                    " --vfs-cache-max-age 72h0m0s" +
                    " --vfs-cache-max-size 512M" +
                    " --vfs-cache-poll-interval 10m0s" +
                    " --cache-dir=" + cacheDir.getPath() +
                    " --log-file " + logFile.getPath() +
                    " --allow-other --gid 1015" +
                    " --daemon";
            Shell.su(cmd);
            Toast.makeText(activity, "rclone mount", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void android_app_Activity__onDestroy__(ILTweaks.MethodParam param) {
        afterOnClass(MEDIA_LIST_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            File dir = new File(getSdcardDir(), "cloud0");
            String umountCmd = "umount " + dir.getPath();
            Shell.su(umountCmd);
            Toast.makeText(activity, "rclone umount", Toast.LENGTH_SHORT).show();
        });
    }
}
