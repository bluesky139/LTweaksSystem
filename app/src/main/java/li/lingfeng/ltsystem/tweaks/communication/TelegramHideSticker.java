package li.lingfeng.ltsystem.tweaks.communication;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.TELEGRAM, prefs = R.string.key_telegram_hide_sticker)
public class TelegramHideSticker extends TweakBase {

    private static final String CHAT_MESSAGE_CALL = "org.telegram.ui.Cells.ChatMessageCell";

    @Override
    public void android_view_View__setMeasuredDimension__int_int(ILTweaks.MethodParam param) {
        beforeOnClass(CHAT_MESSAGE_CALL, param, () -> {
            Object messageObject = ReflectUtils.getObjectField(param.thisObject, "currentMessageObject");
            if (messageObject == null) {
                Logger.w("currentMessageObject is null.");
                return;
            }
            if ((boolean) ReflectUtils.callMethod(messageObject, "isSticker")) {
                Logger.v("Hide sticker.");
                param.setArg(1, 1);
            }
        });
    }
}
