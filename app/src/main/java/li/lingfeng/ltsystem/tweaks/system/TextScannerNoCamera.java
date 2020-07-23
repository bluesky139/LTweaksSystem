package li.lingfeng.ltsystem.tweaks.system;

import android.app.Activity;
import android.content.Intent;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.TEXT_SCANNER, prefs = R.string.key_text_scanner_no_camera)
public class TextScannerNoCamera extends TweakBase {

    private static final String CAMERA_ACTIVITY = "com.peace.TextScanner.CameraActivity";
    private static final String RESULT_ACTIVITY = "com.peace.TextScanner.ResultActivity";
    private boolean mFromShare;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        beforeOnClass(CAMERA_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            mFromShare = Intent.ACTION_SEND.equals(activity.getIntent().getAction());

            // Reset trial or manually clear data if you want.
            activity.getSharedPreferences("info", 0).edit()
                    .putInt("activeCount", 0)
                    .putInt("count", 0)
                    .putInt("readCount", 0)
                    .putInt("freeScanNum", 20)
                    .commit();
        });
    }

    @Override
    public void android_hardware_Camera__open__int(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (mFromShare) {
                Logger.d("No camera from share.");
                param.setResult(null);
            }
        });
    }

    @Override
    public void android_app_Activity__startActivityForResult__Intent_int_Bundle(ILTweaks.MethodParam param) {
        afterOnClass(CAMERA_ACTIVITY, param, () -> {
            Intent intent = (Intent) param.args[0];
            if (mFromShare && intent.getComponent() != null && RESULT_ACTIVITY.equals(intent.getComponent().getClassName())) {
                ((Activity) param.thisObject).finish();
            }
        });
    }
}
