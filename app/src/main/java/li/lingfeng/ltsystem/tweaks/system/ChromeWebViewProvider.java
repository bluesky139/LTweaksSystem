package li.lingfeng.ltsystem.tweaks.system;

import android.webkit.WebViewProviderInfo;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_web_chrome_webview_provider)
public class ChromeWebViewProvider extends TweakBase {

    @Override
    public void com_android_server_webkit_SystemImpl__SystemImpl__(ILTweaks.MethodParam param) {
        param.after(() -> {
            Logger.i("Add Chrome into WebView provider list.");
            WebViewProviderInfo info = new WebViewProviderInfo("com.android.chrome",
                    "Chrome", true, false, new String[0]);
            WebViewProviderInfo[] infos = (WebViewProviderInfo[]) ReflectUtils.getObjectField(param.thisObject, "mWebViewProviderPackages");
            WebViewProviderInfo[] newInfos = new WebViewProviderInfo[infos.length + 1];
            System.arraycopy(infos, 0, newInfos, 0, infos.length);
            newInfos[infos.length] = info;
            ReflectUtils.setObjectField(param.thisObject, "mWebViewProviderPackages", newInfos);
        });
    }
}
