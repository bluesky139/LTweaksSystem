package li.lingfeng.ltsystem.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;

import org.apache.commons.lang3.StringUtils;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.activities.ChromeIncognitoActivity;
import li.lingfeng.ltsystem.activities.SelectableTextActivity;
import li.lingfeng.ltsystem.prefs.PackageNames;

/**
 * Created by smallville on 2017/2/18.
 */

public class ShareUtils {

    public static void shareClipWithSnackbar(final Activity activity, ClipData clipData) {
        try {
            if (clipData == null) {
                return;
            }
            final CharSequence text = clipData.getItemCount() > 0 ? clipData.getItemAt(0).getText() : null;
            if (StringUtils.isEmpty(text)) {
                return;
            }
            SimpleSnackbar.make(activity, "Got text", SimpleSnackbar.LENGTH_LONG)
                    .setAction(ContextUtils.getLDrawable(R.drawable.ic_search), (v) -> searchText(activity, text.toString()))
                    .setAction(ContextUtils.getLDrawable(R.drawable.ic_incognito), (v) -> incognitoText(activity, text.toString()))
                    .setAction(ContextUtils.getLDrawable(R.drawable.ic_edit), (v) -> selectText(activity, text.toString()))
                    .setAction(ContextUtils.getLDrawable(R.drawable.abc_ic_menu_share_mtrl_alpha), (v) -> shareText(activity, text.toString()))
                    .show();
        } catch (Throwable e) {
            Logger.e("shareClipWithSnackbar error, " + e);
            Logger.stackTrace(e);
        }
    }

    public static void searchText(Activity activity, String text) {
        String url = Utils.isUrl(text) ? text : "https://www.google.com/search?gws_rd=cr&q=" + Uri.encode(text);
        ContextUtils.startBrowser(activity, url);
    }

    public static void incognitoText(Activity activity, String text) {
        Intent intent = new Intent(Intent.ACTION_PROCESS_TEXT);
        intent.setClassName(PackageNames.L_TWEAKS, ChromeIncognitoActivity.class.getName());
        intent.putExtra(Intent.EXTRA_PROCESS_TEXT, text);
        activity.startActivity(intent);
    }

    public static void shareText(Activity activity, String text) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        activity.startActivity(Intent.createChooser(shareIntent, "Share with..."));
    }

    public static void selectText(Activity activity, String text) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.setClassName(PackageNames.L_TWEAKS, SelectableTextActivity.class.getName());
        activity.startActivity(shareIntent);
    }
}
