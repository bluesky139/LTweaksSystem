package li.lingfeng.ltsystem.tweaks.system;

import android.os.Build;
import android.view.MenuItem;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.ILTweaksMethods;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;
import li.lingfeng.ltsystem.utils.Triple;
import li.lingfeng.ltsystem.utils.Utils;

@MethodsLoad(packages = {}, prefs = {}, excludedPackages = {
        PackageNames.ANDROID, PackageNames.ANDROID_SYSTEM_UI
})
public class TextActions extends ILTweaksMethods {

    @Override
    public void com_android_internal_widget_FloatingToolbar__getVisibleAndEnabledMenuItems__Menu(ILTweaks.MethodParam param) {
        Set<String> savedItems = Prefs.remote().getStringSet(R.string.key_text_actions_set, null);
        if (savedItems == null || savedItems.size() == 0) {
            return;
        }

        param.addHook(new ILTweaks.MethodHook() {
            @Override
            public void after() throws Throwable {
                final Map<String, Triple<Integer, Boolean, String>> savedItemMap = new HashMap<>(savedItems.size());
                for (String savedItem : savedItems) {
                    String[] strs = Utils.splitReach(savedItem, ':', 5);
                    String name = strs[3];
                    String rename = strs[4];
                    int order = Integer.parseInt(strs[0]);
                    boolean block = Boolean.parseBoolean(strs[1]);
                    savedItemMap.put(name.toUpperCase(), new Triple(order, block, rename));
                }

                List<MenuItem> items = (List<MenuItem>) param.getResult();
                for (int i = items.size() - 1; i >= 0; --i) {
                    MenuItem item = items.get(i);
                    String title = item.getTitle().toString().toUpperCase();
                    title = StringUtils.strip(title, "\u200F\u200E ");
                    Triple<Integer, Boolean, String> triple = savedItemMap.get(title);
                    if (triple != null && triple.second) {
                        Logger.d("Remove floating menu " + item.getTitle());
                        items.remove(i);
                    }
                }

                Logger.d("Sort floating menu " + items.hashCode());
                Collections.sort(items, new Comparator<MenuItem>() {
                    @Override
                    public int compare(MenuItem i1, MenuItem i2) {
                        String title1 = i1.getTitle().toString().toUpperCase();
                        title1 = StringUtils.strip(title1, "\u200F\u200E ");
                        Triple<Integer, Boolean, String> triple = savedItemMap.get(title1);
                        Integer order1 = triple == null ? null : triple.first;

                        String title2 = i2.getTitle().toString().toUpperCase();
                        title2 = StringUtils.strip(title2, "\u200F\u200E ");
                        triple = savedItemMap.get(title2);
                        Integer order2 = triple == null ? null : triple.first;

                        if (order1 == null && order2 == null) {
                            return 0;
                        }
                        if (order1 == null) {
                            return 1;
                        }
                        if (order2 == null) {
                            return -1;
                        }
                        return order1 - order2;
                    }
                });

                for (MenuItem item : items) {
                    String title = item.getTitle().toString().toUpperCase();
                    title = StringUtils.strip(title, "\u200F\u200E ");
                    Triple<Integer, Boolean, String> triple = savedItemMap.get(title);
                    if (triple != null && !StringUtils.isBlank(triple.third)) {
                        item.setTitle(triple.third);
                    }
                }

                // Remove title duplicated menu items after sorting and renaming.
                for (int i = items.size() - 1; i > 0; --i) {
                    String title = items.get(i).getTitle().toString().toUpperCase();
                    title = StringUtils.strip(title, "\u200F\u200E ");
                    for (int j = i - 1; j >= 0; --j) {
                        String title2 = items.get(j).getTitle().toString().toUpperCase();
                        title2 = StringUtils.strip(title2, "\u200F\u200E ");
                        if (title.equals(title2)) {
                            Logger.d("Remove duplicated " + title);
                            items.remove(i);
                            break;
                        }
                    }
                }
            }
        });
    }

    @Override
    public void android_widget_Editor$ProcessTextIntentActionsHandler__onInitializeMenu__Menu(ILTweaks.MethodParam param) {
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O_MR1) {
           return;
        }

        param.addHook(new ILTweaks.MethodHook() {
            @Override
            public void before() throws Throwable {
                ReflectUtils.callMethod(param.thisObject, "loadSupportedActivities");
            }
        });
    }
}
