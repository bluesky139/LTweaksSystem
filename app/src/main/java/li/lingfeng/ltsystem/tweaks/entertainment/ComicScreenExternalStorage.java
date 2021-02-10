package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.widget.Toast;

import java.io.File;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.LTHelper;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.Shell;

@MethodsLoad(packages = PackageNames.COMIC_SCREEN, prefs = R.string.key_comic_screen_external_storage)
public class ComicScreenExternalStorage extends TweakBase {

    private static final String COMIC_LIST_ACTIVITY = "com.viewer.comicscreen.ListActivity";
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
            mSdcardDir = LTHelper.currentApplication().getDir("sdcard", 0);
        }
        return mSdcardDir;
    }

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        beforeOnClass(COMIC_LIST_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            File dir = new File(getSdcardDir(), "cloud0");
            if (!dir.exists()) {
                dir.mkdir();
            }
            File cacheDir = new File(getSdcardDir(), "rclone_cache");
            if (!cacheDir.exists()) {
                cacheDir.mkdir();
            }
            File chunkDir = new File(getSdcardDir(), "rclone_cache-backend");
            if (!chunkDir.exists()) {
                chunkDir.mkdir();
            }
            File logFile = new File(getSdcardDir(), "rclone.log");
            if (logFile.exists()) {
                logFile.delete();
            }

            String cmd = "/sbin/rclone mount cloud0: " + dir.getPath() +
                    " --config /sdcard/rclone_comic_screen.conf" +
                    " --max-read-ahead 128k" +
                    " --buffer-size 8M" +
                    " --dir-cache-time 24h" +
                    " --poll-interval 5m" +
                    " --attr-timeout 24h" +
                    " --vfs-cache-mode writes" +
                    " --vfs-read-chunk-size 2M" +
                    " --vfs-read-chunk-size-limit 10M" +
                    " --vfs-cache-max-age 10h0m0s" +
                    " --vfs-cache-max-size 256M" +
                    " --cache-dir=" + cacheDir.getPath() +
                    " --cache-chunk-path " + chunkDir.getPath() +
                    " --cache-chunk-clean-interval 10m0s " +
                    "--log-file " + logFile.getPath() +
                    " --allow-other --gid 1015" +
                    " --daemon";
            Shell.su(cmd);
            Toast.makeText(activity, "rclone mount", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void android_app_Activity__onDestroy__(ILTweaks.MethodParam param) {
        afterOnClass(COMIC_LIST_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            File dir = new File(getSdcardDir(), "cloud0");
            String umountCmd = "umount " + dir.getPath();
            Shell.su(umountCmd);
            Toast.makeText(activity, "rclone umount", Toast.LENGTH_SHORT).show();
        });
    }
}
