package li.lingfeng.ltsystem.tweaks.communication;

import android.graphics.Color;
import android.webkit.WebView;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.TELEGRAM, prefs = R.string.key_telegram_darken)
public class TelegramDarkenVideoBackground extends TweakBase {

    private static final String EMBED_BOTTOM_SHEET = "org.telegram.ui.Components.EmbedBottomSheet";

    @Override
    public void android_app_Dialog__show__(ILTweaks.MethodParam param) {
        beforeOnClass(EMBED_BOTTOM_SHEET, param, () -> {
            Logger.v("Set dark background on webView, in EmbedBottomSheet.");
            WebView webView = (WebView) ReflectUtils.getObjectField(param.thisObject, "webView");
            webView.setBackgroundColor(Color.BLACK);
        });
    }
}
