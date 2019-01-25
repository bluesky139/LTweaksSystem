package li.lingfeng.ltsystem.tweaks.system;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageParser;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ViewUtils;

public class NovaLauncherSearchGoChrome {

    private static final String NOVA_LAUNCHER = "com.teslacoilsw.launcher.NovaLauncher";
    private static final String SEARCH_ACTIVITY = "org.chromium.chrome.browser.searchwidget.SearchActivity";

    @MethodsLoad(packages = PackageNames.NOVA_LAUNCHER, prefs = R.string.key_nova_launcher_search_go_chrome)
    public static class NovaLauncher extends TweakBase {

        @Override
        public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
            afterOnClass(NOVA_LAUNCHER, param, () -> {
                Activity activity = (Activity) param.thisObject;
                activity.getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        boolean ok = true;
                        try {
                            View searchSpace = ViewUtils.findViewByName(activity, "qsb_base_search_space");
                            if (searchSpace != null) {
                                Logger.d("searchSpace " + searchSpace);
                                searchSpace.setOnClickListener((v) -> {
                                    try {
                                        Intent intent = new Intent();
                                        intent.setComponent(new ComponentName(PackageNames.CHROME, SEARCH_ACTIVITY));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                                        activity.startActivity(intent);
                                    } catch (Throwable e) {
                                        Logger.e("Launch chrome search activity failed.", e);
                                        Toast.makeText(activity, "Launch chrome search activity failed.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                ok = false;
                            }
                        } catch (Throwable e) {
                            Logger.e("Handle nova search exception.", e);
                        }
                        if (ok) {
                            activity.getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                });
            });
        }
    }

    @MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_nova_launcher_search_go_chrome)
    public static class Android extends TweakBase {

        @Override
        public void android_content_pm_PackageParser__parsePackage__File_int_boolean(ILTweaks.MethodParam param) {
            param.after(() -> {
                PackageParser.Package pkg = (PackageParser.Package) param.getResult();
                if (pkg == null || pkg.packageName != PackageNames.CHROME) {
                    return;
                }
                for (PackageParser.Activity activity : pkg.activities) {
                    if (activity.info.name.equals(SEARCH_ACTIVITY)) {
                        Logger.i("Set " + SEARCH_ACTIVITY + " exported to true.");
                        activity.info.exported = true;
                        break;
                    }
                }
            });
        }
    }
}
