package li.lingfeng.ltsystem.tweaks.communication;

import java.util.List;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.TELEGRAM, prefs = {})
public class TelegramMessageFilter extends TweakBase {

    private static final String CHAT_MESSAGE_CALL = "org.telegram.ui.Cells.ChatMessageCell";
    private List<String> mWords;

    @Override
    public void android_view_View__setMeasuredDimension__int_int(ILTweaks.MethodParam param) {
        beforeOnClass(CHAT_MESSAGE_CALL, param, () -> {
            if (!loadWords()) {
                return;
            }
            Object messageObject = ReflectUtils.getObjectField(param.thisObject, "currentMessageObject");
            if (messageObject == null) {
                Logger.w("currentMessageObject is null.");
                return;
            }

            boolean contains = false;
            CharSequence messageText = (CharSequence) ReflectUtils.getObjectField(messageObject, "messageText");
            //Logger.d("messageText " + messageText);
            if (containsWords(messageText)) {
                contains = true;
            } else {
                CharSequence caption = (CharSequence) ReflectUtils.getObjectField(messageObject, "caption");
                //Logger.d("caption " + caption);
                if (containsWords(caption)) {
                    contains = true;
                } else {
                    CharSequence linkDescription = (CharSequence) ReflectUtils.getObjectField(messageObject, "linkDescription");
                    if (linkDescription != null) {
                        //Logger.d("linkDescription " + linkDescription);
                        if (containsWords(linkDescription.toString())) {
                            contains = true;
                        } else {
                            Object messageOwner = ReflectUtils.getObjectField(messageObject, "messageOwner");
                            Object media = ReflectUtils.getObjectField(messageOwner, "media");
                            if (media != null) {
                                Object webpage = ReflectUtils.getObjectField(media, "webpage");
                                if (webpage != null) {
                                    String siteName = (String) ReflectUtils.getObjectField(webpage, "site_name");
                                    //Logger.d("siteName " + siteName);
                                    if (containsWords(siteName)) {
                                        contains = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (contains) {
                Logger.v("Hide annoying message " + messageObject.hashCode());
                param.setArg(1, 1);
            }
        });
    }

    private boolean loadWords() {
        mWords = Prefs.large().getStringList(R.string.key_telegram_message_filter, null);
        return mWords != null && mWords.size() > 0;
    }

    private boolean containsWords(CharSequence text) {
        if (text == null) {
            return false;
        }
        for (String word : mWords) {
            if (text.toString().contains(word)) {
                return true;
            }
        }
        return false;
    }
}
