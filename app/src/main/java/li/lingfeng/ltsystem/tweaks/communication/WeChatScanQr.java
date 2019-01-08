package li.lingfeng.ltsystem.tweaks.communication;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.LTHelper;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.ClassNames;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.WE_CHAT, prefs = R.string.key_system_share_qrcode_scan)
public class WeChatScanQr extends TweakBase {

    private static final String BASE_SCAN_UI = "com.tencent.mm.plugin.scanner.ui.BaseScanUI";
    private String mScannableImage = null;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        beforeOnClass(ClassNames.WE_CHAT_LAUNCHER_UI, param, () -> {
            final Activity activity = (Activity) param.thisObject;
            mScannableImage = activity.getIntent().getStringExtra("ltweaks_scannable_image");
            Logger.i("ltweaks_scannable_image " + mScannableImage);
        });

        afterOnClass(BASE_SCAN_UI, param, () -> {
            if (StringUtils.isEmpty(mScannableImage)) {
                return;
            }

            final Activity activity = (Activity) param.thisObject;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        scanImage(activity, mScannableImage);
                    } catch (Throwable e) {
                        Logger.e("scanImage error, " + e);
                        Logger.stackTrace(e);
                    } finally {
                        mScannableImage = null;
                    }
                }
            }, 1000);
        });
    }

    @Override
    public void android_app_Activity__performNewIntent__Intent(ILTweaks.MethodParam param) {
        beforeOnClass(ClassNames.WE_CHAT_LAUNCHER_UI, param, () -> {
            Intent intent = (Intent) param.args[0];
            mScannableImage = intent.getStringExtra("ltweaks_scannable_image");
            Logger.i("ltweaks_scannable_image " + mScannableImage);
        });
    }

    private void scanImage(Activity activity, String imagePath) throws Throwable {
        Logger.i("scanImage " + imagePath);
        Intent intent = new Intent();
        intent.setData(Uri.fromFile(new File(imagePath)));
        ReflectUtils.callMethod(activity, "onActivityResult",
                new Object[] { 4660, -1, intent },
                new Class[] { int.class, int.class, Intent.class });
    }
}
