package li.lingfeng.ltsystem.tweaks.system;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ShareUtils;

@MethodsLoad(packages = {}, prefs = R.string.key_system_share_copy_to_share, excludedPackages = {
        PackageNames.ANDROID, PackageNames.ANDROID_SYSTEM_UI
})
public class CopyToShare extends TweakBase {

    private boolean mListenerAdded = false;
    private WeakReference<Activity> mActivityRef;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        if (mListenerAdded) {
            return;
        }
        param.after(() -> {
            ClipboardManager clipboardManager = (ClipboardManager) ((Activity) param.thisObject).getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.addPrimaryClipChangedListener(() -> {
                Optional.ofNullable(mActivityRef)
                        .filter(Objects::nonNull)
                        .map(WeakReference::get)
                        .filter(Objects::nonNull)
                        .ifPresent((a) -> {
                            a.runOnUiThread(() -> {
                                new Handler().postDelayed(() -> {
                                    if (mActivityRef == null) {
                                        return;
                                    }
                                    Activity activity = mActivityRef.get();
                                    if (activity != null) {
                                        ClipData clipData = clipboardManager.getPrimaryClip();
                                        Logger.i("ClipboardManager onPrimaryClipChanged " + clipData);
                                        ShareUtils.shareClipWithSnackbar(activity, clipData);
                                    }
                                }, 500);
                            });
                        });
            });
            mListenerAdded = true;
        });
    }

    @Override
    public void android_app_Activity__onResume__(ILTweaks.MethodParam param) {
        param.after(() -> {
            Activity activity = (Activity) param.thisObject;
            mActivityRef = new WeakReference<>(activity);
        });
    }

    @Override
    public void android_app_Activity__onPause__(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (mActivityRef == null) {
                return;
            }
            Activity activity = (Activity) param.thisObject;
            if (mActivityRef.get() == activity) {
                mActivityRef = null;
            }
        });
    }
}
