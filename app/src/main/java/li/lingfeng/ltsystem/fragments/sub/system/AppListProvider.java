package li.lingfeng.ltsystem.fragments.sub.system;

import android.content.pm.ApplicationInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.activities.ListCheckActivity;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.utils.Logger;

public class AppListProvider extends ListCheckActivity.DataProvider {

    public class AppInfo {
        public ApplicationInfo mInfo;
        public boolean mDisabled;

        public AppInfo(ApplicationInfo info, boolean disabled) {
            mInfo = info;
            mDisabled = disabled;
        }
    }

    private int mKey;
    private Map<String, AppInfo> mMapAllInfos = new TreeMap<>();
    private Map<String, AppInfo> mMapDisabledInfos = new TreeMap<>();
    private Map<String, AppInfo> mMapEnabledInfos = new TreeMap<>();
    private List<AppInfo> mAllInfos;
    private List<AppInfo> mDisabledInfos;
    private List<AppInfo> mEnabledInfos;
    private List<String> mDisabledApps;
    private boolean mNeedReload = true;

    public AppListProvider(ListCheckActivity activity) {
        super(activity);
        mKey = activity.getIntent().getIntExtra("key", 0);
        mDisabledApps = Prefs.large().getStringList(mKey, new ArrayList<>());
        List<ApplicationInfo> infos = mActivity.getPackageManager().getInstalledApplications(0);
        for (ApplicationInfo info : infos) {
            AppInfo appInfo = new AppInfo(info, mDisabledApps.contains(info.packageName));
            mMapAllInfos.put(info.packageName, appInfo);
            if (appInfo.mDisabled) {
                mMapDisabledInfos.put(info.packageName, appInfo);
            } else {
                mMapEnabledInfos.put(info.packageName, appInfo);
            }
        }
        reload();
    }

    @Override
    protected String getActivityTitle() {
        return mActivity.getIntent().getStringExtra("title");
    }

    @Override
    protected String[] getTabTitles() {
        return new String[] {
                mActivity.getString(R.string.all),
                mActivity.getString(R.string.disabled),
                mActivity.getString(R.string.enabled)
        };
    }

    @Override
    protected int getListItemCount(int tab) {
        if (tab == 0) {
            return mAllInfos.size();
        } else if (tab == 1) {
            return mDisabledInfos.size();
        } else if (tab == 2) {
            return mEnabledInfos.size();
        } else {
            throw new RuntimeException("Unknown tab " + tab);
        }
    }

    @Override
    protected ListItem getListItem(int tab, int position) {
        List<AppInfo> infos;
        if (tab == 0) {
            infos = mAllInfos;
        } else if (tab == 1) {
            infos = mDisabledInfos;
        } else if (tab == 2) {
            infos = mEnabledInfos;
        } else {
            throw new RuntimeException("Unknown tab " + tab);
        }

        ListItem item = new ListItem();
        final AppInfo appInfo = infos.get(position);
        item.mData = appInfo;
        item.mIcon = appInfo.mInfo.loadIcon(mActivity.getPackageManager());
        item.mTitle = appInfo.mInfo.loadLabel(mActivity.getPackageManager());
        item.mDescription = appInfo.mInfo.packageName;
        item.mChecked = appInfo.mDisabled;
        return item;
    }

    @Override
    protected boolean reload() {
        if (!mNeedReload) {
            return false;
        }

        mNeedReload = false;
        mAllInfos = new ArrayList<>(mMapAllInfos.values());
        mDisabledInfos = new ArrayList<>(mMapDisabledInfos.values());
        mEnabledInfos = new ArrayList<>(mMapEnabledInfos.values());
        Logger.d("mAllInfos " + mAllInfos.size() + ", mDisabledInfos " + mDisabledInfos.size() + ", mEnabledInfos " + mEnabledInfos.size());
        return true;
    }

    @Override
    public void onCheckedChanged(ListCheckActivity.DataProvider.ListItem item, Boolean isChecked) {
        AppInfo appInfo = (AppInfo) item.mData;
        String packageName = appInfo.mInfo.packageName;
        Logger.i((isChecked ? "Disabled" : "Enabled") + " no root " + packageName);

        appInfo.mDisabled = isChecked;
        if (isChecked) {
            mMapDisabledInfos.put(packageName, appInfo);
            mMapEnabledInfos.remove(packageName);
            mDisabledApps.add(packageName);
        } else {
            mMapDisabledInfos.remove(packageName);
            mMapEnabledInfos.put(packageName, appInfo);
            mDisabledApps.remove(packageName);
        }
        mNeedReload = true;

        Prefs.large().putStringList(mKey, mDisabledApps);
    }
}

