package li.lingfeng.ltsystem.fragments.sub.system;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.alibaba.fastjson.JSONArray;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.activities.ListCheckActivity;
import li.lingfeng.ltsystem.prefs.IntentActions;
import li.lingfeng.ltsystem.prefs.PrefKeys;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.ListMultiChoiceDialog;
import li.lingfeng.ltsystem.utils.PackageUtils;

import static li.lingfeng.ltsystem.utils.PackageUtils.SORT_BY_DATE;
import static li.lingfeng.ltsystem.utils.PackageUtils.SORT_BY_NAME;

/**
 * Created by smallville on 2017/10/31.
 */

public class PreventListDataProvider extends ListCheckActivity.DataProvider {

    private static final int[] KEYS = {
            R.string.key_prevent_list_prevent_exact_alarm,
            R.string.key_prevent_list_prevent_receiver
    };

    class AppInfo {
        PackageInfo packageInfo;
        boolean[] prevented;

        boolean isPrevented() {
            for (boolean b : prevented) {
                if (b) {
                    return true;
                }
            }
            return false;
        }

        boolean allPrevented() {
            for (boolean b : prevented) {
                if (!b) {
                    return false;
                }
            }
            return true;
        }
    }

    private JSONArray[] mPreventSets;
    private AppInfo[] mAppInfos;
    private List<AppInfo> mPreventedAppInfos;
    private List<AppInfo> mFreeAppInfos;
    private boolean mNeedReload = false;
    private int mSort = SORT_BY_NAME;

    public PreventListDataProvider(ListCheckActivity activity) {
        super(activity);
        mPreventSets = new JSONArray[KEYS.length];
        for (int i = 0; i < KEYS.length; ++i) {
            mPreventSets[i] = Prefs.largeEditor().getArray(KEYS[i], new JSONArray());
        }

        List<PackageInfo> packages = PackageUtils.getInstalledPackages();
        PackageUtils.sortPackages(packages, mSort);
        mAppInfos = new AppInfo[packages.size()];
        mPreventedAppInfos = new ArrayList<>();
        mFreeAppInfos = new ArrayList<>();
        for (int i = 0; i < mAppInfos.length; ++i) {
            AppInfo appInfo = new AppInfo();
            appInfo.packageInfo = packages.get(i);
            appInfo.prevented = new boolean[KEYS.length];
            for (int j = 0; j < KEYS.length; ++j) {
                appInfo.prevented[j] = mPreventSets[j].contains(appInfo.packageInfo.packageName);
            }
            mAppInfos[i] = appInfo;
            if (appInfo.isPrevented()) {
                mPreventedAppInfos.add(appInfo);
            } else {
                mFreeAppInfos.add(appInfo);
            }
        }
    }

    @Override
    protected String getActivityTitle() {
        return mActivity.getString(R.string.pref_prevent_running_set_list);
    }

    @Override
    protected String[] getTabTitles() {
        return new String[] {
                mActivity.getString(R.string.prevent_running_list_all),
                mActivity.getString(R.string.prevent_running_list_prevented),
                mActivity.getString(R.string.prevent_running_list_not_prevented)
        };
    }

    @Override
    protected int getListItemCount(int tab) {
        switch (tab) {
            case 0:
                return mAppInfos.length;
            case 1:
                return mPreventedAppInfos.size();
            case 2:
                return mFreeAppInfos.size();
        }
        throw new RuntimeException("Unknown tab " + tab);
    }

    @Override
    protected ListItem getListItem(int tab, int position) {
        AppInfo appInfo;
        switch (tab) {
            case 0:
                appInfo = mAppInfos[position];
                break;
            case 1:
                appInfo = mPreventedAppInfos.get(position);
                break;
            case 2:
                appInfo = mFreeAppInfos.get(position);
                break;
            default:
                throw new RuntimeException("Unknown tab " + tab);
        }
        ListItem item = new ListItem();
        item.mData = appInfo;
        item.mIcon = appInfo.packageInfo.applicationInfo.loadIcon(mActivity.getPackageManager());
        item.mTitle = appInfo.packageInfo.applicationInfo.loadLabel(mActivity.getPackageManager());
        item.mDescription = appInfo.packageInfo.packageName;
        if (appInfo.isPrevented()) {
            if (appInfo.allPrevented()) {
                item.mChecked = true;
            } else {
                item.mChecked = null;
            }
        } else {
            item.mChecked = false;
        }
        return item;
    }

    @Override
    protected boolean reload() {
        if (mNeedReload) {
            mNeedReload = false;
            return true;
        }
        return false;
    }

    @Override
    public void onCheckedChanged(ListItem item, Boolean isChecked) {
        if (isChecked != null) {
            boolean[] prevented = new boolean[KEYS.length];
            Arrays.fill(prevented, isChecked);
            updatePrevented(item.getData(AppInfo.class), prevented);
        }
    }

    private void updatePrevented(AppInfo appInfo, boolean[] prevented) {
        appInfo.prevented = prevented;
        if (appInfo.isPrevented()) {
            if (!mPreventedAppInfos.contains(appInfo)) {
                mPreventedAppInfos.add(appInfo);
                sortAppInfos(mPreventedAppInfos);
            }
            mFreeAppInfos.remove(appInfo);
        } else {
            if (!mFreeAppInfos.contains(appInfo)) {
                mFreeAppInfos.add(appInfo);
                sortAppInfos(mFreeAppInfos);
            }
            mPreventedAppInfos.remove(appInfo);
        }

        for (int i = 0; i < KEYS.length; ++i) {
            JSONArray set = mPreventSets[i];
            if (prevented[i]) {
                if (!set.contains(appInfo.packageInfo.packageName)) {
                    set.add(appInfo.packageInfo.packageName);
                }
            } else {
                set.remove(appInfo.packageInfo.packageName);
            }
            Prefs.largeEditor().putArray(KEYS[i], set);
        }
        mNeedReload = true;
    }

    private void sortAppInfos(List<AppInfo> appInfos) {
        final PackageManager packageManager = mActivity.getPackageManager();
        Collections.sort(appInfos, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
                if (mSort == SORT_BY_NAME) {
                    return o1.packageInfo.applicationInfo.loadLabel(packageManager).toString().compareTo(
                            o2.packageInfo.applicationInfo.loadLabel(packageManager).toString());
                } else if (mSort == SORT_BY_DATE) {
                    return (int) (o1.packageInfo.firstInstallTime - o2.packageInfo.firstInstallTime);
                } else  {
                    throw new RuntimeException("sortAppInfos() unknown sort " + mSort);
                }
            }
        });
    }

    @Override
    protected boolean linkItemClickToCheckBox() {
        return false;
    }

    @Override
    public void onItemClick(ListItem item) {
        ListMultiChoiceDialog.Item[] choiceItems = new ListMultiChoiceDialog.Item[KEYS.length];
        for (int i = 0; i < KEYS.length; ++i) {
            ListMultiChoiceDialog.Item choiceItem = new ListMultiChoiceDialog.Item();
            String key = PrefKeys.getById(KEYS[i]);
            String title = key.replace("key_prevent_list", "pref_prevent_running");
            choiceItem.text = ContextUtils.getString(title);

            if (KEYS[i] != R.string.key_prevent_list_prevent_receiver) {
                String summary = title + "_summary";
                choiceItem.summary = ContextUtils.getString(summary);
            } else {
                choiceItem.summary = StringUtils.join(IntentActions.sReceiverPreventedArray, '\n');
            }
            choiceItems[i] = choiceItem;
        }
        final AppInfo appInfo = item.getData(AppInfo.class);
        new ListMultiChoiceDialog(mActivity, item.mTitle, choiceItems, appInfo.prevented.clone(),
                new ListMultiChoiceDialog.OnMultiChoicesChangeListener() {
            @Override
            public void onChange(boolean[] choices) {
                updatePrevented(appInfo, choices);
                notifyDataSetChanged();
            }
        }).show();
    }
}
