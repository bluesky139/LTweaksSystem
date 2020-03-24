package li.lingfeng.ltsystem.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.utils.Logger;

/**
 * Created by lilingfeng on 2017/7/11.
 */

public class BilibiliActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String url = getIntent().getDataString();
        Logger.i("Bilibili url " + url);

        if (!start(this, url)) {
            Toast.makeText(this, R.string.not_supported, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    public static boolean start(Context context, String url) {
        // http://m.bilibili.com/video/av123.html
        // https://b23.tv/av123
        String[] regs = new String[] {
                "https?://m\\.bilibili\\.com/video/(av\\d+)(\\.html)?(\\?p=(\\d+))?",
                "https?://m\\.bilibili\\.com/video/(BV\\w+)(\\.html)?(\\?p=(\\d+))?",
                "https?://b23\\.tv/(av\\d+)(/p(\\d+))?",
                "https?://b23\\.tv/(BV\\w+)(/p(\\d+))?"
        };
        for (String reg : regs) {
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                Logger.i("Got video id " + matcher.group(1));
                Intent intent = new Intent(Intent.ACTION_VIEW);
                url = "https://www.bilibili.com/video/" + matcher.group(1);
                String page = matcher.group(matcher.groupCount());
                if (page != null) {
                    Logger.i("Got page " + page);
                    url += "?p=" + page;
                }
                intent.setData(Uri.parse(url));
                intent.setPackage(PackageNames.BILIBILI);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("from_ltweaks", true);
                context.startActivity(intent);
                return true;
            }
        }
        return false;
    }
}
