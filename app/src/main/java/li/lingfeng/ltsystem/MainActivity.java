package li.lingfeng.ltsystem;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.alibaba.fastjson.JSON;

import java.util.List;

import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.PermissionUtils;
import li.lingfeng.ltsystem.utils.ViewUtils;

public class MainActivity extends PreferenceActivity {

    private AlertDialog mGettingLargeStoreDialog;
    private GettingLargeStoreReceiver mGettingLargeStoreReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.FLAVOR.equals("selfUse")) {
            setTitle(getTitle() + " - Self Use");
        }
        getNewestLargeStore();
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
        if (BuildConfig.FLAVOR.equals("selfUse")) {
            loadHeadersFromResource(ContextUtils.getXmlId("pref_headers_self_use"), target);
        }
    }

    private void getNewestLargeStore() {
        mGettingLargeStoreDialog = ViewUtils.showProgressingDialog(this, false, null);
        mGettingLargeStoreReceiver = new GettingLargeStoreReceiver();
        registerReceiver(mGettingLargeStoreReceiver, new IntentFilter(Prefs.LARGE_STORE_ALL));
        Intent intent = new Intent(Prefs.LARGE_STORE_GET);
        sendBroadcast(intent);
    }

    class GettingLargeStoreReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.i("Received newest large store values.");
            String content = intent.getStringExtra("all");
            Prefs.largeEditor().setLargeStore(JSON.parseObject(content));
            unregisterReceiver(this);
            mGettingLargeStoreDialog.dismiss();
            mGettingLargeStoreDialog = null;
            mGettingLargeStoreReceiver = null;
        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return fragmentName.startsWith(PackageNames.L_TWEAKS + ".fragments.");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}
