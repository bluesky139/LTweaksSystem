package li.lingfeng.ltsystem.services;

import com.crossbowffs.remotepreferences.RemotePreferenceProvider;

/**
 * Created by lilingfeng on 2017/8/16.
 */

public class MainPreferenceProvider extends RemotePreferenceProvider {

    public MainPreferenceProvider() {
        super("li.lingfeng.ltsystem.mainpreferences", new String[] { "large_store" });
    }

    @Override
    protected boolean checkAccess(String prefName, String prefKey, boolean write) {
        return !write;
    }
}
