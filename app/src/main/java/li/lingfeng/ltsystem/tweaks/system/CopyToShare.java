package li.lingfeng.ltsystem.tweaks.system;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;

import java.lang.ref.WeakReference;

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
        param.addHook(new ILTweaks.MethodHook() {
            @Override
            public void after() throws Throwable {
                ClipboardManager clipboardManager = (ClipboardManager) ((Activity) param.thisObject).getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.addPrimaryClipChangedListener(() -> {
                    mListenerAdded = true;
                    if (mActivityRef == null) {
                        return;
                    }
                    Activity activity = mActivityRef.get();
                    if (activity == null) {
                        return;
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (mActivityRef == null) {
                                        return;
                                    }
                                    Activity activity = mActivityRef.get();
                                    if (activity != null) {
                                        ClipData clipData = clipboardManager.getPrimaryClip();
                                        Logger.i("ClipboardManager onPrimaryClipChanged " + clipData);
                                        ShareUtils.shareClipWithSnackbar(activity, clipData);
                                    }
                                }
                            }, 500);
                        }
                    });
                });
            }
        });
    }

    @Override
    public void android_app_Activity__onResume__(ILTweaks.MethodParam param) {
        param.addHook(new ILTweaks.MethodHook() {
            @Override
            public void after() throws Throwable {
                Activity activity = (Activity) param.thisObject;
                mActivityRef = new WeakReference<>(activity);
            }
        });
    }

    @Override
    public void android_app_Activity__onPause__(ILTweaks.MethodParam param) {
        param.addHook(new ILTweaks.MethodHook() {
            @Override
            public void before() throws Throwable {
                if (mActivityRef == null) {
                    return;
                }
                Activity activity = (Activity) param.thisObject;
                if (mActivityRef.get() == activity) {
                    mActivityRef = null;
                }
            }
        });
    }
}
