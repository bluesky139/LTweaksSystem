package li.lingfeng.ltsystem.tweaks.communication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.ClassNames;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.WE_CHAT, prefs = R.string.key_wechat_browser)
public class WeChatBrowser extends TweakBase {

    private static final String WEBVIEW_MP_UI = "com.tencent.mm.plugin.webview.ui.tools.WebviewMpUI";

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(ClassNames.WE_CHAT_LAUNCHER_UI, param, () -> {
            final Activity activity = (Activity) param.thisObject;
            String url = activity.getIntent().getStringExtra("ltweaks_open_url");
            if (url != null) {
                openBrowser(activity, url);
            }
        });
    }

    @Override
    public void android_app_Activity__performNewIntent__Intent(ILTweaks.MethodParam param) {
        afterOnClass(ClassNames.WE_CHAT_LAUNCHER_UI, param, () -> {
            Activity activity = (Activity) param.thisObject;
            Intent intent = (Intent) param.args[0];
            String url = intent.getStringExtra("ltweaks_open_url");
            if (url != null) {
                openBrowser(activity, url);
            }
        });
    }

    private void openBrowser(Activity activity, String url) {
        Logger.v("Open browser in WeChat " + url);
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(PackageNames.WE_CHAT, WEBVIEW_MP_UI));
        intent.putExtra("rawUrl", url);
        intent.putExtra("geta8key_scene", 3);
        intent.putExtra("useJs", true);
        intent.putExtra("vertical_scroll", true);
        activity.startActivity(intent);
    }
}
