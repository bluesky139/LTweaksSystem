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
            Logger.i("Add Google WebView into webview provider list.");
            WebViewProviderInfo info = new WebViewProviderInfo("com.google.android.webview",
                    "Google WebView", true, false, new String[] {
                            "MIIDuzCCAqOgAwIBAgIJANi6DgBQG4ZTMA0GCSqGSIb3DQEBBQUAMHQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIDApDYWxpZm9ybmlhMRYwFAYDVQQHDA1Nb3VudGFpbiBWaWV3MRQwEgYDVQQKDAtHb29nbGUgSW5jLjEQMA4GA1UECwwHQW5kcm9pZDEQMA4GA1UEAwwHd2VidmlldzAeFw0xNDA4MDgyMzIwMjBaFw00MTEyMjQyMzIwMjBaMHQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIDApDYWxpZm9ybmlhMRYwFAYDVQQHDA1Nb3VudGFpbiBWaWV3MRQwEgYDVQQKDAtHb29nbGUgSW5jLjEQMA4GA1UECwwHQW5kcm9pZDEQMA4GA1UEAwwHd2VidmlldzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMbtaFX0r5aZJMAbPVMAgK1ZZ29dTn91VsGxXv2hqrQo7IpqEy2JmPvPnoMsSiuTAe+UcQy8oKDQ2aYVSAd1DGIy+nSRyFTt3LSIAdwSBkB1qT4a+OqkpsR6bSNXQXQ18lCQu9gREY3h3QlYBQAyzRxw4hRGlrXAzuSz1Ec4W+6x4nLG5DG61MAMR8ClF9XSqbmGB3kyZ70A0X9OPYYxiMWP1ExaYvpaVqjyZZcrPwr+vtW8oCuGBUtHpBUH3OoG+9s2YMcgLG7vCK9awKDqlPcJSpIAAj6uGs4gORmkqxZRMskLSTWbhP4p+3Ap8jYzTVB6Y1/DMVmYTWRMcPW0macCAwEAAaNQME4wHQYDVR0OBBYEFJ6bAR6/QVm4w9LRSGQiaR5Rhp3TMB8GA1UdIwQYMBaAFJ6bAR6/QVm4w9LRSGQiaR5Rhp3TMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQEFBQADggEBAEQu8QiVxax7/diEiJrgKE1LwdXsIygJK/KnaKdnYEkAQpeu/QmrLiycm+OFbL1qHJIB7OuI/PQBUtcaNSiJSCVgtwtEbZWWIdsynqG/Nf4aGOndXegSQNRH54M05sRHLoeRycPrY7xQlEwGikNFR76+5UdwFBQI3Gn22g6puJnVukQm/wXQ+ajoiS4QclrNlixoDQsZ4STLH4+Wju2wIWKFFArIhVEIlbamq+p6BghuzH3aIz/Fy0YTQKi7SA+0fuNeCaqlSm5pYSt6p5CH89y1Fr+wFc5r3iLRnUwRcy08ESC7bZJnxV3d/YQ5valTxBbzku/dQbXVj/xg69H8l8M"
            });
            WebViewProviderInfo[] infos = (WebViewProviderInfo[]) ReflectUtils.getObjectField(param.thisObject, "mWebViewProviderPackages");
            WebViewProviderInfo[] newInfos = new WebViewProviderInfo[infos.length + 1];
            System.arraycopy(infos, 0, newInfos, 0, infos.length);
            newInfos[infos.length] = info;
            ReflectUtils.setObjectField(param.thisObject, "mWebViewProviderPackages", newInfos);
        });
    }
}
