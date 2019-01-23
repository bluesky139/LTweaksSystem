package li.lingfeng.ltsystem.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import li.lingfeng.ltsystem.prefs.ClassNames;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.utils.Logger;

public class WeChatBrowserActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String url = getIntent().getDataString();
        Logger.i("WeChatBrowserActivity url " + url);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName(PackageNames.WE_CHAT, ClassNames.WE_CHAT_LAUNCHER_UI);
        intent.putExtra("ltweaks_open_url", url);
        intent.setFlags(335544320);
        startActivity(intent);

        finish();
    }
}
