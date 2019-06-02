package li.lingfeng.ltsystem.tweaks.communication;

import android.app.Dialog;
import android.content.DialogInterface;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.TELEGRAM, prefs = R.string.key_telegram_open_url_without_confirmation)
public class TelegramOpenUrlWithoutConfirmation extends TweakBase {

    private static final String ALERT_DIALOG = "org.telegram.ui.ActionBar.AlertDialog";

    @Override
    public void android_app_Dialog__show__(ILTweaks.MethodParam param) {
        beforeOnClass(ALERT_DIALOG, param, () -> {
            Dialog dialog = (Dialog) param.thisObject;
            String title = ReflectUtils.getObjectField(dialog, "title").toString();
            String message = ReflectUtils.getObjectField(dialog, "message").toString();
            if (title.equals("Open Link") && message.startsWith("Do you want to open ")) {
                Logger.v(message + " Yes.");
                DialogInterface.OnClickListener listener = (DialogInterface.OnClickListener)
                        ReflectUtils.getObjectField(dialog, "positiveButtonListener");
                listener.onClick(dialog, -1);
                param.setResult(null);
            }
        });
    }
}
