package li.lingfeng.ltsystem.tweaks.communication;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ScrollView;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.TT_RSS, prefs = R.string.key_ttrss_open_link_in_browser_menu)
public class TTRssArticleOpenInBrowserMenu extends TweakBase {

    private static final String DETAIL_ACTIVITY = "org.fox.ttrss.DetailActivity";
    private static final int MENU_OPEN_IN_BROWSER_ID = 10000;
    private static final int MENU_COPY_TITLE_ID = 10001;
    private static final int MENU_SHARE_ID = 10002;
    private static final int MENU_GO_TOP_ID = 10003;
    private static final int MENU_GO_BOTTOM_ID = 10004;

    @Override
    public void android_app_Activity__onCreatePanelMenu__int_Menu(ILTweaks.MethodParam param) {
        afterOnCreateOptionsMenu(DETAIL_ACTIVITY, param, () -> {
            Menu menu = (Menu) param.args[1];
            if (menu != null && menu.findItem(MENU_OPEN_IN_BROWSER_ID) == null) {
                Logger.i("Create open in browser menu.");
                int idMenuGroup = ContextUtils.getIdId("menu_group_article");

                MenuItem menuItem = menu.add(idMenuGroup, MENU_OPEN_IN_BROWSER_ID, Menu.NONE, ContextUtils.getLString(R.string.ttrss_open_in_browser));
                menuItem.setIcon(ContextUtils.getDrawable("ic_action_web_site"));
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

                menu.add(idMenuGroup, MENU_COPY_TITLE_ID, Menu.NONE, ContextUtils.getLString(R.string.ttrss_copy_title));
                menu.add(idMenuGroup, MENU_SHARE_ID, Menu.NONE, ContextUtils.getString("share_share_button"));
                menu.add(idMenuGroup, MENU_GO_TOP_ID, Menu.NONE, ContextUtils.getLString(R.string.ttrss_go_top));
                menu.add(idMenuGroup, MENU_GO_BOTTOM_ID, Menu.NONE, ContextUtils.getLString(R.string.ttrss_go_bottom));

                menu.findItem(ContextUtils.getIdId("toggle_published")).setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            }
        });
    }

    @Override
    public void android_app_Activity__onMenuItemSelected__int_MenuItem(ILTweaks.MethodParam param) {
        beforeOnOptionsItemSelected(DETAIL_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            MenuItem menuItem = (MenuItem) param.args[1];
            switch (menuItem.getItemId()) {
                case MENU_OPEN_IN_BROWSER_ID: {
                    Logger.i("Open link in browser.");
                    Object article = getArticleFromActivity(activity);
                    String link = (String) ReflectUtils.getObjectField(article, "link");
                    ReflectUtils.callMethod(param.thisObject, "openUri",
                            new Object[] { Uri.parse(link) },
                            new Class[] { Uri.class });
                    break;
                }
                case MENU_COPY_TITLE_ID: {
                    Logger.i("Copy title.");
                    Object article = getArticleFromActivity(activity);
                    String title = (String) ReflectUtils.getObjectField(article, "title");
                    ClipboardManager clipboardManager = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("", title));
                    break;
                }
                case MENU_SHARE_ID: {
                    Logger.i("Share.");
                    Object article = getArticleFromActivity(activity);
                    ReflectUtils.callMethod(activity, "shareArticle", article);
                    break;
                }
                case MENU_GO_TOP_ID: {
                    Logger.i("Go top.");
                    ScrollView scrollView = getScrollViewFromActivity(activity);
                    scrollView.scrollTo(0, 0);
                    break;
                }
                case MENU_GO_BOTTOM_ID: {
                    Logger.i("Go bottom.");
                    ScrollView scrollView = getScrollViewFromActivity(activity);
                    scrollView.scrollTo(0, scrollView.getChildAt(0).getHeight());
                    break;
                }
                default:
                    return;
            }
            param.setResult(true);
        });
    }

    private Object getArticleFromActivity(Activity activity) throws Throwable {
        Object fragmentManager = ReflectUtils.callMethod(activity, "getSupportFragmentManager");
        Object articlePager = ReflectUtils.callMethod(fragmentManager, "findFragmentByTag", "article");
        return ReflectUtils.callMethod(articlePager, "getSelectedArticle");
    }

    private WebView getWebViewFromActivity(Activity activity) throws Throwable {
        Object fragmentManager = ReflectUtils.callMethod(activity, "getSupportFragmentManager");
        Object articlePager = ReflectUtils.callMethod(fragmentManager, "findFragmentByTag", "article");
        Object pagerAdapter = ReflectUtils.getObjectField(articlePager, "m_adapter");
        Object fragment = ReflectUtils.callMethod(pagerAdapter, "getCurrentFragment");
        return (WebView) ReflectUtils.getObjectField(fragment, "m_web");
    }

    private ScrollView getScrollViewFromActivity(Activity activity) throws Throwable {
        Object fragmentManager = ReflectUtils.callMethod(activity, "getSupportFragmentManager");
        Object articlePager = ReflectUtils.callMethod(fragmentManager, "findFragmentByTag", "article");
        Object pagerAdapter = ReflectUtils.getObjectField(articlePager, "m_adapter");
        Object fragment = ReflectUtils.callMethod(pagerAdapter, "getCurrentFragment");
        return (ScrollView) ReflectUtils.getObjectField(fragment, "m_contentView");
    }
}
