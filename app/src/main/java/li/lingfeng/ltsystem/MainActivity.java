package li.lingfeng.ltsystem;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import java.util.List;

import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.utils.ContextUtils;

public class MainActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.FLAVOR.equals("selfUse")) {
            setTitle(getTitle() + " - Self Use");
        }
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
        if (BuildConfig.FLAVOR.equals("selfUse")) {
            loadHeadersFromResource(ContextUtils.getXmlId("pref_headers_self_use"), target);
        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return fragmentName.startsWith(PackageNames.L_TWEAKS + ".fragments.");
    }
}
