package li.lingfeng.ltsystem.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.prefs.ClassNames;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.utils.ComponentUtils;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.PackageUtils;

/**
 * Created by lilingfeng on 2017/6/30.
 */

public class ProcessTextActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!getIntent().getAction().equals(Intent.ACTION_PROCESS_TEXT)
                || !getIntent().getType().equals("text/plain")
                || !ComponentUtils.isAlias(this)) {
            Toast.makeText(this, R.string.not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String text = getIntent().getStringExtra(Intent.EXTRA_PROCESS_TEXT);
        if (text == null || text.isEmpty()) {
            Toast.makeText(this, R.string.not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String name = ComponentUtils.getAlias(this);
        Logger.i("ProcessText " + text + " with " + name);
        try {
            Method method = ProcessTextActivity.class.getDeclaredMethod(StringUtils.uncapitalize(name), String.class);
            method.invoke(this, text);
        } catch (Exception e) {
            Logger.e("ProcessTextActivity invoke error, " + e);
            Logger.stackTrace(e);
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Nullable
    @Override
    public Uri getReferrer() {
        int clipUid = getIntent().getIntExtra("ltweaks_clip_uid", 0);
        if (clipUid > 0) {
            return Uri.parse("android-app://" + getPackageManager().getNameForUid(clipUid));
        }
        return super.getReferrer();
    }

    private void douban(String text) {
        Intent intent = new Intent();
        intent.setClassName(PackageNames.DOUBAN, ClassNames.DOUBAN_SEARCH_ACTIVITY);
        //intent.putExtra("query_type", "subject");
        intent.putExtra("query", text);
        //intent.putExtra("search_show_result", true);
        intent.putExtra("from_ltweaks", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_REFERRER, getReferrer());
        startActivity(intent);
    }

    private void bilibili(String text) {
        Intent intent = new Intent(Intent.ACTION_SEARCH);
        intent.setClassName(PackageUtils.isPackageInstalled(PackageNames.BILIBILI_IN)
                ? PackageNames.BILIBILI_IN : PackageNames.BILIBILI, ClassNames.BILIBILI_SEARCH_ACTIVITY);
        intent.putExtra("query", text);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_REFERRER, getReferrer());
        startActivity(intent);
    }

    private void webSearch(String text) {
        String url = "https://www.google.com/search?q=" + Uri.encode(text);
        boolean fromChrome = false;
        int clipUid = getIntent().getIntExtra("ltweaks_clip_uid", 0);
        if (clipUid > 0 && PackageNames.CHROME.equals(getPackageManager().getNameForUid(clipUid))) {
            fromChrome = true;
        }
        ContextUtils.startBrowser(this, url, fromChrome);
    }
}
