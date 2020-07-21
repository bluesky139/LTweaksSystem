package li.lingfeng.ltsystem.tweaks.communication;

import android.view.View;

import java.util.List;
import java.util.WeakHashMap;

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
    private WeakHashMap<Object, Boolean> mMessageCache = new WeakHashMap<>();

    @Override
    public void android_view_View__setMeasuredDimension__int_int(ILTweaks.MethodParam param) {
        beforeOnClass(CHAT_MESSAGE_CALL, param, () -> {
            View view = (View) param.thisObject;
            if (!loadWords()) {
                setVisible(view, true);
                return;
            }
            Object messageObject = ReflectUtils.getObjectField(view, "currentMessageObject");
            if (messageObject == null) {
                Logger.w("currentMessageObject is null.");
                setVisible(view, true);
                return;
            }

            boolean contains;
            Boolean _contains = mMessageCache.get(messageObject);
            if (_contains != null) {
                //Logger.d("got cache " + messageObject);
                contains = _contains;
            } else {
                contains = messageContainsWords(messageObject);
                Object messageGroup = ReflectUtils.getObjectField(view, "currentMessagesGroup");
                if (messageGroup != null) {
                    List messages = (List) ReflectUtils.getObjectField(messageGroup, "messages");
                    if (!contains) {
                        for (Object message : messages) {
                            if (message == messageObject) {
                                continue;
                            }
                            contains = messageContainsWords(message);
                            if (contains) {
                                break;
                            }
                        }
                    }
                    Boolean cacheContains = Boolean.valueOf(contains);
                    messages.forEach(m -> mMessageCache.put(m, cacheContains));
                } else {
                    mMessageCache.put(messageObject, contains);
                }
            }

            if (contains) {
                Logger.v("Hide annoying message " + messageObject.hashCode());
                param.setArg(1, 1);
            }
            setVisible(view, !contains);
        });
    }

    private boolean loadWords() {
        mWords = Prefs.large().getStringList(R.string.key_telegram_message_filter, null);
        return mWords != null && mWords.size() > 0;
    }

    private boolean messageContainsWords(Object messageObject) throws Throwable {
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
        return contains;
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

    private void setVisible(View view, boolean visible) {
        if (view.getVisibility() == View.VISIBLE && !visible
                || view.getVisibility() == View.INVISIBLE && visible) {
            view.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
