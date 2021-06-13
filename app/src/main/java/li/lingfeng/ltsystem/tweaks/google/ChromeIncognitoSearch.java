package li.lingfeng.ltsystem.tweaks.google;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.LTHelper;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.IntentActions;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.Utils;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = {
        PackageNames.CHROME,
        PackageNames.CHROME_BETA,
        PackageNames.CHROME_DEV,
        PackageNames.CHROME_CANARY,
}, prefs = R.string.key_chrome_incognito_search)
public class ChromeIncognitoSearch extends ChromeBase {

    private static final String CONTEXT_MENU_PARAMS = "org.chromium.chrome.browser.contextmenu.ContextMenuParams";

    // Menu "Open in incognito" in Chrome and CustomTab.
    @Override
    protected Map<String, MenuInfo> newMenus() {
        Map<String, MenuInfo> infos = new HashMap<>(1);
        String title = ContextUtils.getLString(R.string.chrome_open_in_incognito);
        infos.put(title, new MenuInfo(title, 1001, (activity, url, isCustomTab) -> {
            Intent intent = new Intent(IntentActions.ACTION_CHROME_INCOGNITO);
            intent.setData(Uri.parse(url));
            intent.putExtra("from_ltweaks_external", isCustomTab);
            intent.putExtra("chrome_package_for_ltweaks", getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            activity.startActivity(intent);
        }));
        return infos;
    }

    // Fake trusted app code to allow open incognito tab from outside.
    @Override
    public void android_content_Intent__getParcelableExtra__String(ILTweaks.MethodParam param) {
        param.before(() -> {
            String key = (String) param.args[0];
            Intent intent = (Intent) param.thisObject;
            if (key.equals("trusted_application_code_extra") && intent.getBooleanExtra("from_ltweaks", false)) {
                Logger.d("Return fake trusted_application_code_extra.");
                Intent intent2 = new Intent();
                intent2.setComponent(new ComponentName(LTHelper.currentApplication(), "FakeClass"));
                PendingIntent pendingIntent = PendingIntent.getActivity(LTHelper.currentApplication(),
                        0, intent2, PendingIntent.FLAG_IMMUTABLE);
                param.setResult(pendingIntent);
            }
        });
    }

    // Context menu "Open in incognito" in CustomTab.
    @Override
    public void com_android_internal_view_menu_ContextMenuBuilder__showDialog__View_IBinder(ILTweaks.MethodParam param) {
        param.after(() -> {
            ContextMenu menu = (ContextMenu) param.thisObject;
            if (IntStream.range(0, menu.size())
                    .mapToObj(i -> menu.getItem(i))
                    .filter(item -> "Copy link address".equals(item.getTitle()))
                    .count() > 0) {
                View view = (View) param.args[0];
                View.OnCreateContextMenuListener listener = ViewUtils.getViewCreateContextMenuListener(view);
                Activity activity = (Activity) Utils.findFirstFieldByExactType(listener.getClass(), Activity.class).get(listener);
                if (!isCustomTab(activity)) {
                    return;
                }

                MenuItem item = addMenu(menu, ContextUtils.getLString(R.string.chrome_open_in_incognito), 1001, true);
                if (item != null) {
                    item.setOnMenuItemClickListener((_item) -> {
                        try {
                            String url = getCurrentUrl(activity);
                            onMenuItemClick(url);
                        } catch (Throwable e) {
                            Logger.e("Context menu incognito search click exception.", e);;
                        }
                        return true;
                    });
                }
            }
        });
    }

    private void onMenuItemClick(String linkUrl) {
        Logger.i("Open link in incognito: " + linkUrl);
        Intent intent = new Intent(IntentActions.ACTION_CHROME_INCOGNITO);
        intent.setData(Uri.parse(linkUrl));
        intent.putExtra("chrome_package_for_ltweaks", getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        LTHelper.currentApplication().startActivity(intent);
    }
}
