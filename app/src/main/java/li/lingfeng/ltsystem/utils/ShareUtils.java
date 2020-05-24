package li.lingfeng.ltsystem.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;

import java.io.File;

import li.lingfeng.ltsystem.activities.ChromeIncognitoActivity;
import li.lingfeng.ltsystem.activities.SelectableTextActivity;
import li.lingfeng.ltsystem.prefs.PackageNames;

/**
 * Created by smallville on 2017/2/18.
 */

public class ShareUtils {

    public static void searchText(Context context, String text, int referrerUid) {
        String url = Utils.isUrl(text) ? text : "https://www.google.com/search?gws_rd=cr&q=" + Uri.encode(text);
        boolean fromChrome = false;
        if (referrerUid > 0) {
            fromChrome = PackageNames.CHROME.equals(context.getPackageManager().getNameForUid(referrerUid));
        }
        ContextUtils.startBrowser(context, url, fromChrome);
    }

    public static void incognitoText(Context context, String text) {
        Intent intent = new Intent(Intent.ACTION_PROCESS_TEXT);
        intent.setClassName(PackageNames.L_TWEAKS, ChromeIncognitoActivity.class.getName());
        intent.putExtra(Intent.EXTRA_PROCESS_TEXT, text);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void shareText(Context context, String text) {
        shareText(context, text, 0);
    }

    public static void shareText(Context context, String text, int referrerUid) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        Logger.d("context " + context);
        Intent intent = Intent.createChooser(shareIntent, "Share with...");
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (referrerUid > 0) {
            shareIntent.putExtra("ltweaks_clip_uid", referrerUid);
        }
        context.startActivity(intent);
    }

    public static void selectText(Context context, String text, int referrerUid) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.setClassName(PackageNames.L_TWEAKS, SelectableTextActivity.class.getName());
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (referrerUid > 0) {
            shareIntent.putExtra("ltweaks_clip_uid", referrerUid);
        }
        context.startActivity(shareIntent);
    }

    public static void shareImage(Context context, File file) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        context.startActivity(Intent.createChooser(shareIntent, "Share with..."));
    }

    public static void shareVideo(Context context, String path) {
        shareVideo(context, new File(path));
    }

    public static void shareVideo(Context context, File file) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("video/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        context.startActivity(Intent.createChooser(shareIntent, "Share with..."));
    }
}
