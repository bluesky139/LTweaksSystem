package li.lingfeng.ltsystem.tweaks.shopping;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SafeBrowsingResponse;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ShoppingUtils;

@MethodsLoad(packages = PackageNames.SMZDM, prefs = R.string.key_smzdm_open_link_in_app)
public class SMZDMOpenLinkInApp extends TweakBase {

    private static final String INNER_BROWSER_ACTIVITY = "com.smzdm.client.android.extend.InnerBrowser.InnerBrowserActivity";
    private static final String JD_WEBVIEW_ACTIVITY = "com.kepler.jd.sdk.WebViewActivity";

    private Activity mActivity;
    private HashMap<WebView, WebViewClient> mWebViewClients;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        param.before(() -> {
            String clsName = param.thisObject.getClass().getName();
            if (clsName.equals(INNER_BROWSER_ACTIVITY) || clsName.equals(JD_WEBVIEW_ACTIVITY)) {
                if (mActivity != null) {
                    Logger.e("mActivity is not null, " + mActivity + ", new " + param.thisObject);
                }
                mActivity = (Activity) param.thisObject;
            }
        });
    }

    @Override
    public void android_app_Activity__onDestroy__(ILTweaks.MethodParam param) {
        param.after(() -> {
            if (mActivity == param.thisObject) {
                mActivity = null;
                mWebViewClients = null;
            }
        });
    }

    @Override
    public void android_webkit_WebView__setWebViewClient__WebViewClient(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (mWebViewClients == null) {
                mWebViewClients = new HashMap<>();
            }
            WebView webView = (WebView) param.thisObject;
            WebViewClient originalClient = (WebViewClient) param.args[0];
            if (originalClient == null) {
                mWebViewClients.remove(webView);
            }

            WebViewClient middleClient = new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Logger.d("shouldOverrideUrlLoading " + url);
                    if (handleUrl(url)) {
                        return true;
                    }
                    return originalClient.shouldOverrideUrlLoading(view, url);
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    String url = request.getUrl().toString();
                    Logger.d("shouldOverrideUrlLoading " + url);
                    if (handleUrl(url)) {
                        return true;
                    }
                    return originalClient.shouldOverrideUrlLoading(view, request);
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    originalClient.onPageStarted(view, url, favicon);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    originalClient.onPageFinished(view, url);
                }

                @Override
                public void onLoadResource(WebView view, String url) {
                    originalClient.onLoadResource(view, url);
                }

                @Override
                public void onPageCommitVisible(WebView view, String url) {
                    originalClient.onPageCommitVisible(view, url);
                }

                @Nullable
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                    return originalClient.shouldInterceptRequest(view, url);
                }

                @Nullable
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    return originalClient.shouldInterceptRequest(view, request);
                }

                @Override
                public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {
                    originalClient.onTooManyRedirects(view, cancelMsg, continueMsg);
                }

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    originalClient.onReceivedError(view, errorCode, description, failingUrl);
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    originalClient.onReceivedError(view, request, error);
                }

                @Override
                public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                    originalClient.onReceivedHttpError(view, request, errorResponse);
                }

                @Override
                public void onFormResubmission(WebView view, Message dontResend, Message resend) {
                    originalClient.onFormResubmission(view, dontResend, resend);
                }

                @Override
                public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                    originalClient.doUpdateVisitedHistory(view, url, isReload);
                }

                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    originalClient.onReceivedSslError(view, handler, error);
                }

                @Override
                public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
                    originalClient.onReceivedClientCertRequest(view, request);
                }

                @Override
                public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                    originalClient.onReceivedHttpAuthRequest(view, handler, host, realm);
                }

                @Override
                public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
                    return originalClient.shouldOverrideKeyEvent(view, event);
                }

                @Override
                public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
                    originalClient.onUnhandledKeyEvent(view, event);
                }

                @Override
                public void onScaleChanged(WebView view, float oldScale, float newScale) {
                    originalClient.onScaleChanged(view, oldScale, newScale);
                }

                @Override
                public void onReceivedLoginRequest(WebView view, String realm, @Nullable String account, String args) {
                    originalClient.onReceivedLoginRequest(view, realm, account, args);
                }

                @Override
                public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                    return originalClient.onRenderProcessGone(view, detail);
                }

                @Override
                public void onSafeBrowsingHit(WebView view, WebResourceRequest request, int threatType, SafeBrowsingResponse callback) {
                    originalClient.onSafeBrowsingHit(view, request, threatType, callback);
                }
            };
            param.setArg(0, middleClient);
            mWebViewClients.put(webView, originalClient);
        });
    }

    @Override
    public void android_webkit_WebView__getWebViewClient__(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (mWebViewClients == null) {
                return;
            }
            WebView webView = (WebView) param.thisObject;
            WebViewClient originalClient = mWebViewClients.get(webView);
            if (originalClient != null) {
                param.setResult(originalClient);
            }
        });
    }

    private boolean handleUrl(String url) {
        if (mActivity == null) {
            return true;
        }
        String itemId = ShoppingUtils.findItemIdByStore(url, ShoppingUtils.STORE_JD);
        if (itemId != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("openapp.jdmobile://virtual?params={\"category\":\"jump\",\"des\":\"productDetail\",\"skuId\":\"" + itemId + "\",\"sourceType\":\"Item\",\"sourceValue\":\"view-ware\"}"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mActivity.startActivity(intent);
            mActivity.finish();
            Logger.v("InnerBrowser finished.");
            return true;
        }
        return false;
    }
}
