package li.lingfeng.ltsystem.tweaks.system;

import android.graphics.Rect;
import android.net.Uri;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.internal.view.menu.MenuBuilder;

import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.ILTweaksMethods;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = {}, prefs = R.string.key_selection_action_mode_original, excludedPackages = {
        PackageNames.ANDROID, PackageNames.ANDROID_SYSTEM_UI
})
public class OriginalSelectionActionMode extends ILTweaksMethods {

    @Override
    public void android_widget_TextView__setCustomSelectionActionModeCallback__ActionMode$Callback(ILTweaks.MethodParam param) {
        param.before(() -> {
            ActionMode.Callback original = (ActionMode.Callback) param.args[0];
            Logger.i("setCustomSelectionActionModeCallback middle callback for " + original);
            if (original != null && !original.getClass().getName().startsWith(PackageNames.L_TWEAKS)) {
                param.setArg(0, new MiddleCallback((TextView) param.thisObject, original));
            }
        });
    }

    @Override
    public void android_widget_TextView__canProcessText__(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (ReflectUtils.getObjectField(param.thisObject, "mEditor") != null) {
                Logger.i("canProcessText return true");
                param.setResult(true);
            } else {
                Logger.d("No mEditor for canProcessText.");
            }
        });
    }

    @Override
    public void android_view_View__startActionMode__ActionMode$Callback_int(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (param.thisObject instanceof TextView && (int) param.args[1] == ActionMode.TYPE_FLOATING) {
                TextView textView = (TextView) param.thisObject;
                if (textView.getCustomSelectionActionModeCallback() == null) {
                    textView.setCustomSelectionActionModeCallback(new MiddleCallback(textView, null));
                }
            }
        });
    }

    private class MiddleCallback extends ActionMode.Callback2 {

        private TextView mTextView;
        private ActionMode.Callback mOriginal;
        private Map<CharSequence, MenuItem> mOriginalItems;
        private boolean mAddedWebSearch = false;

        MiddleCallback(TextView textView, ActionMode.Callback original) {
            mTextView = textView;
            mOriginal = original;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            try {
                if (mOriginal != null) {
                    mOriginalItems = new LinkedHashMap<>();
                    for (int i = 0; i < menu.size(); ++i) {
                        MenuItem item = menu.getItem(i);
                        if (!StringUtils.isEmpty(item.getTitle())) {
                            mOriginalItems.put(item.getTitle(), item);
                        }
                    }

                    menu.clear();
                    List internalItems = (List) ReflectUtils.getObjectField(menu, "mItems");
                    mOriginal.onPrepareActionMode(mode, menu);
                    Iterator<Map.Entry<CharSequence, MenuItem>> it = mOriginalItems.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<CharSequence, MenuItem> kv = it.next();
                        boolean exist = false;
                        for (int i = 0; i < menu.size(); ++i) {
                            if (kv.getKey().equals(menu.getItem(i).getTitle())) {
                                exist = true;
                                break;
                            }
                        }
                        if (!exist) {
                            internalItems.add(kv.getValue());
                        } else {
                            it.remove();
                        }
                    }
                    ((MenuBuilder) menu).onItemsChanged(true);
                }

                boolean hasWebSearch = false;
                for (int i = 0; i < menu.size(); ++i) {
                    if (menu.getItem(i).getTitle().toString().toLowerCase().equals("web search")) {
                        hasWebSearch = true;
                        break;
                    }
                }
                if (!hasWebSearch) {
                    mAddedWebSearch = true;
                    Logger.d("Add \"Web Search\".");
                    menu.add("Web Search");
                }
            } catch (Throwable e) {
                Logger.e("MiddleCallback.onPrepareActionMode() exception.", e);
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (mAddedWebSearch && "Web Search".equals(item.getTitle())) {
                Logger.d("\"Web Search\" clicked.");
                try {
                    int selStart = mTextView.getSelectionStart();
                    int selEnd = mTextView.getSelectionEnd();
                    int min = Math.max(0, Math.min(selStart, selEnd));
                    int max = Math.max(0, Math.max(selStart, selEnd));
                    String text = ReflectUtils.callMethod(mTextView, "getTransformedText",
                            new Object[] { min, max }, new Class[] { int.class, int.class }).toString();
                    String url = "https://www.google.com/search?q=" + Uri.encode(text);
                    ContextUtils.startBrowser(mTextView.getContext(), url);
                } catch (Throwable e) {
                    Logger.e("\"Web Search\" click exception.", e);
                }
            }
            return mOriginal == null || mOriginalItems.containsKey(item.getTitle()) ? false : mOriginal.onActionItemClicked(mode, item);
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (mOriginal != null) {
                mOriginal.onDestroyActionMode(mode);
            }
        }

        @Override
        public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
            if (mOriginal != null && mOriginal instanceof ActionMode.Callback2) {
                ((ActionMode.Callback2) mOriginal).onGetContentRect(mode, view, outRect);
            }
        }
    }
}
