package li.lingfeng.ltsystem.tweaks.communication;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.activities.BilibiliActivity;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.WE_CHAT, prefs = R.string.key_wechat_external_browser)
public class WeChatExternalBrowser extends TweakBase {

    private static final String WEBVIEW_UI = "com.tencent.mm.plugin.webview.ui.tools.WebViewUI";
    private static final String APP_BRAND_UI = "com.tencent.mm.plugin.appbrand.ui.AppBrandUI";

    @Override
    public void android_app_Activity__startActivityForResult__Intent_int_Bundle(ILTweaks.MethodParam param) {
        param.before(() -> {
            Intent intent = (Intent) param.args[0];
            if (intent.getBooleanExtra("from_ltweaks", false)) {
                return;
            }
            Activity activity = (Activity) param.thisObject;
            if (intent.getComponent() != null) {
                if (intent.getComponent().getClassName().equals(WEBVIEW_UI)) {
                    String url = intent.getStringExtra("rawUrl");
                    if (url != null) {
                        if (BilibiliActivity.start(activity, url)) {
                            param.setResult(null);
                            return;
                        }
                        String[] packageNames = new String[] {
                                PackageNames.BILIBILI,
                                PackageNames.DOUBAN
                        };
                        for (String packageName : packageNames) {
                            Intent intentToResolve = new Intent(Intent.ACTION_VIEW);
                            intentToResolve.setPackage(packageName);
                            intentToResolve.setData(Uri.parse(url));
                            ResolveInfo resolveInfo = activity.getPackageManager().resolveActivity(intentToResolve, 0);
                            if (resolveInfo != null) {
                                Logger.v("Url " + url + " resolved by " + resolveInfo);
                                intentToResolve.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intentToResolve.putExtra("from_ltweaks", true);
                                activity.startActivity(intentToResolve);
                                param.setResult(null);
                                return;
                            }
                        }
                    }
                } else if (intent.getComponent().getClassName().startsWith(APP_BRAND_UI)) {
                    String config = intent.getExtras().get("key_appbrand_init_config").toString();
                    Pattern pattern = Pattern.compile("appId='wx7564fd5313d24844'.+enterPath='pages\\/.+\\.html\\?((av)?(ep)?)id=(\\d+).*?(&page=(\\d+))?");
                    Matcher matcher = pattern.matcher(config);
                    if (matcher.find()) {
                        String category = matcher.group(1);
                        String videoId = matcher.group(4);
                        Logger.v("Got bilibili " + category + "id " + videoId + " from app brand.");
                        intent = new Intent(Intent.ACTION_VIEW);
                        intent.setPackage(PackageNames.BILIBILI);
                        String url = category.equals("av") ? "https://www.bilibili.com/video/av" + videoId
                                : "https://www.bilibili.com/bangumi/play/ep" + videoId;
                        String page = matcher.group(6);
                        if (page != null) {
                            page = String.valueOf(Integer.parseInt(page) + 1);
                            Logger.v("Got page " + page);
                            url += "?p=" + page;
                        }
                        intent.setData(Uri.parse(url));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("from_ltweaks", true);
                        activity.startActivity(intent);
                        param.setResult(null);
                    } else {
                        pattern = Pattern.compile("appId='wx2f9b06c1de1ccfca'.+enterPath='pages\\/subject\\/subject\\.html\\?type=(\\w+)&id=(\\d+)");
                        matcher = pattern.matcher(config);
                        if (matcher.find()) {
                            String type = matcher.group(1);
                            String id = matcher.group(2);
                            Logger.v("Got douban " + type + " id " + id + " from app brand.");
                            if (type.equals("tv")) {
                                type = "movie";
                            }
                            intent = new Intent(Intent.ACTION_VIEW);
                            intent.setPackage(PackageNames.DOUBAN);
                            intent.setData(Uri.parse("https://" + type + ".douban.com/subject/" + id + "/"));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("from_ltweaks", true);
                            activity.startActivity(intent);
                            param.setResult(null);
                        }
                    }
                }
            }
        });
    }
}
