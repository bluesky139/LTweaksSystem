package li.lingfeng.ltsystem.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.utils.ComponentUtils;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;

public class GoMarketActivity extends Activity {

    private static final String ACTION_GO_MARKET = PackageNames.L_TWEAKS + ".ACTION_GO_MARKET";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String text = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        if (StringUtils.isEmpty(text)) {
            Toast.makeText(this, R.string.not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!ComponentUtils.isAlias(this)) {
            Logger.i("Choose market.");
            Intent intent = new Intent(ACTION_GO_MARKET);
            intent.setType(getIntent().getType());
            intent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(intent, "Choose market..."));
        } else {
            String market = ComponentUtils.getAlias(this);
            Pattern pattern = Pattern.compile("\\?id=([a-zA-Z0-9_\\.]+)");
            Matcher matcher = pattern.matcher(text);
            if (!matcher.find()) {
                Logger.d("No valid package name, " + text);
                Toast.makeText(this, R.string.not_supported, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            String packageName = matcher.group(1);
            Logger.i("Go market " + market + " with " + packageName);
            goMarket(market, packageName);
        }
        finish();
    }

    private void goMarket(String menuName, String packageName) {
        try {
            if ("AppInfo".equals(menuName)) {
                ContextUtils.openAppInfo(this, packageName);
            } else if ("Mobilism".equals(menuName)) {
                //ContextUtils.searchInMobilism(this, appInfo.loadLabel(getPackageManager()));
            } else if ("ApkMirror".equals(menuName)) {
                ContextUtils.searchInApkMirror(this, packageName);
            } else {
                Map<String, String> marketNameToPackage = new HashMap<String, String>() {{
                    put("CoolApk", PackageNames.COOLAPK);
                    put("ApkPure", PackageNames.APKPURE);
                }};
                String market = marketNameToPackage.get(menuName);
                if (market != null) {
                    ContextUtils.openAppInMarket(this, packageName, market);
                } else {
                    throw new Exception("Unknown menu " + menuName);
                }
            }
        } catch (Throwable e) {
            Logger.e("goMarket exception.", e);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
