package li.lingfeng.ltsystem.tweaks.system;

import android.view.MenuItem;

import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
}, hiddenApiExemptions = "Lcom/android/internal/widget/FloatingToolbar;")
public class TextActions extends ILTweaksMethods {

    private Comparator<MenuItem> mMenuItemComparator;
    private Comparator<MenuItem> mEmptyComparator;

    @Override
    public void com_android_internal_widget_FloatingToolbar__getVisibleAndEnabledMenuItems__Menu(ILTweaks.MethodParam param) {
        List<String> savedItems = Prefs.large().getStringList(R.string.key_text_actions_set, null);
        if (savedItems == null || savedItems.size() == 0) {
            return;
        }

        param.after(() -> {
            final Map<String, Triple<Integer, Boolean, String>> savedItemMap = new HashMap<>(savedItems.size());
            for (Iterator it = savedItems.iterator(); it.hasNext(); ) {
                String savedItem = (String) it.next();
                String[] strs = Utils.splitReach(savedItem, ':', 5);
                String name = strs[3];
                String rename = strs[4];
                int order = Integer.parseInt(strs[0]);
                boolean block = Boolean.parseBoolean(strs[1]);
                savedItemMap.put(name.toUpperCase(), new Triple(order, block, rename));
                //Logger.d("savedItem " + name + ", " + order + ", " + block + ", " + rename);
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
            if (mMenuItemComparator == null) {
                mMenuItemComparator = (i1, i2) -> {
                    // Ensure the assist menu item is always the first item:
                    if (i1.getItemId() == android.R.id.textAssist) {
                        return i2.getItemId() == android.R.id.textAssist ? 0 : -1;
                    }
                    if (i2.getItemId() == android.R.id.textAssist) {
                        return 1;
                    }

                    String title1 = i1.getTitle().toString().toUpperCase();
                    title1 = StringUtils.strip(title1, "\u200F\u200E ");
                    Triple<Integer, Boolean, String> triple = savedItemMap.get(title1);
                    Integer order1 = triple == null ? null : triple.first;

                    String title2 = i2.getTitle().toString().toUpperCase();
                    title2 = StringUtils.strip(title2, "\u200F\u200E ");
                    triple = savedItemMap.get(title2);
                    Integer order2 = triple == null ? null : triple.first;

                    //Logger.d("title1 " + title1 + "[" + order1 + "], title2 " + title2 + "[" + order2 + "]");
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
                };
                mEmptyComparator = (i1, i2) -> { return 0; };
            }
            items.sort(mMenuItemComparator);
            ReflectUtils.setObjectField(param.thisObject, "mMenuItemComparator", mEmptyComparator);

            for (MenuItem item : items) {
                String title = item.getTitle().toString().toUpperCase();
                title = StringUtils.strip(title, "\u200F\u200E ");
                Triple<Integer, Boolean, String> triple = savedItemMap.get(title);
                if (triple != null && !StringUtils.isBlank(triple.third)) {
                    item.setTitle(triple.third);
                }
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
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

            /*for (MenuItem item : items) {
                Logger.d("final item " + item.getTitle());
            }*/
        });
    }
}
