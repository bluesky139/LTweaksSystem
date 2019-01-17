package li.lingfeng.ltsystem.tweaks.shopping;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.WE_CHAT, prefs = R.string.key_jd_mini_program_price_history)
public class JDMiniProgramCopyShareLink extends TweakBase {

    private static final String APP_BRAND_UI = "com.tencent.mm.plugin.appbrand.ui.AppBrandUI";
    private static final String SELECT_CONVERSATION_UI = "com.tencent.mm.ui.transmit.SelectConversationUI";

    @Override
    public void android_app_Activity__startActivityForResult__Intent_int_Bundle(ILTweaks.MethodParam param) {
        param.before(() -> {
            Intent intent = (Intent) param.args[0];
            if (intent.getComponent() == null || !intent.getComponent().getClassName().equals(SELECT_CONVERSATION_UI)
                    || !param.thisObject.getClass().getName().startsWith(APP_BRAND_UI)) {
                return;
            }
            HashMap map = (HashMap) intent.getSerializableExtra("appbrand_params");
            String appId = (String) map.get("app_id");
            if (!appId.equals("wx91d27dbf599dff74")) {
                return;
            }

            String path = (String) map.get("path");
            Matcher matcher = Pattern.compile(".+sku=(\\d+)").matcher(path);
            if (!matcher.find()) {
                Logger.e("Can't find item id in " + path);
                return;
            }
            String itemId = matcher.group(1);
            Logger.i("JD item id " + itemId);

            Activity activity = (Activity) param.thisObject;
            ClipboardManager clipboardManager = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, "https://item.jd.com/" + itemId + ".html"));
            param.setResult(null);
        });
    }
}
