package li.lingfeng.ltsystem.tweaks.system;

import android.content.Intent;
import android.graphics.Rect;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.internal.view.menu.MenuBuilder;

import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;


public class OriginalSelectionActionMode {

    @MethodsLoad(packages = {}, prefs = R.string.key_selection_action_mode_original, excludedPackages = {
            PackageNames.ANDROID, PackageNames.ANDROID_SYSTEM_UI
    })
    public static class All extends TweakBase {

        @Override
        public void android_widget_TextView__setCustomSelectionActionModeCallback__ActionMode$Callback(ILTweaks.MethodParam param) {
            param.before(() -> {
                ActionMode.Callback original = (ActionMode.Callback) param.args[0];
                Logger.i("setCustomSelectionActionModeCallback middle callback for " + original);
                param.setArg(0, new MiddleCallback(original));
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

        private static class MiddleCallback extends ActionMode.Callback2 {

            private ActionMode.Callback mOriginal;
            private Map<CharSequence, MenuItem> mOriginalItems;

            MiddleCallback(ActionMode.Callback original) {
                mOriginal = original;
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                try {
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
                } catch (Throwable e) {
                    Logger.e("MiddleCallback.onPrepareActionMode() exception.", e);
                }
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return mOriginalItems.containsKey(item.getTitle()) ? false : mOriginal.onActionItemClicked(mode, item);
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mOriginal.onDestroyActionMode(mode);
            }

            @Override
            public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
                if (mOriginal instanceof ActionMode.Callback2) {
                    ((ActionMode.Callback2) mOriginal).onGetContentRect(mode, view, outRect);
                }
            }
        }
    }

    @MethodsLoad(packages = PackageNames.CHROME, prefs = R.string.key_selection_action_mode_original)
    public static class Chrome extends TweakBase {

        // Chrome treat LTweaks as browser and items set to invisible, let them visible again.
        // https://chromium.googlesource.com/chromium/src/+/refs/tags/83.0.4103.106/chrome/android/java/src/org/chromium/chrome/browser/ChromeActionModeHandler.java#124
        @Override
        public void com_android_internal_widget_FloatingToolbar__getVisibleAndEnabledMenuItems__Menu(ILTweaks.MethodParam param) {
            param.before(() -> {
                Menu menu = (Menu) param.args[0];
                for (int i = 0; i < menu.size(); ++i) {
                    MenuItem item = menu.getItem(i);
                    Intent intent = item.getIntent();
                    if (intent != null && intent.getComponent() != null
                            && PackageNames.L_TWEAKS.equals(intent.getComponent().getPackageName())) {
                        item.setVisible(true);
                    }
                }
            });
        }
    }
}
