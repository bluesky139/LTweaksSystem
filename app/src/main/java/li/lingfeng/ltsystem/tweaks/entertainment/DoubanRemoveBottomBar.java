package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.SimpleDrawer;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.DOUBAN, prefs = R.string.key_douban_remove_bottom_bar)
public class DoubanRemoveBottomBar extends TweakBase {

    private static final String MAIN_ACTIVITY = "com.douban.frodo.MainActivity";
    private static final String MAIN_TAB_ITEM = "com.douban.frodo.view.MainTabItem";
    private SimpleDrawer mDrawerLayout;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(MAIN_ACTIVITY, param, () -> {
            final Activity activity = (Activity) param.thisObject;
            new Handler().post(() -> {
                try {
                    hookBottomBar(activity);
                } catch (Throwable e) {
                    Logger.e("Can't hookBottomBar.");
                    Logger.stackTrace(e);
                }
            });
        });
    }

    @Override
    public void android_app_Activity__onDestroy__(ILTweaks.MethodParam param) {
        afterOnClass(MAIN_ACTIVITY, param, () -> {
            mDrawerLayout = null;
        });
    }

    @Override
    public void android_app_Activity__onKeyUp__int_KeyEvent(ILTweaks.MethodParam param) {
        beforeOnBackPressed(MAIN_ACTIVITY, param, () -> {
            if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                Logger.i("Back is pressed for closing drawer.");
                mDrawerLayout.closeDrawers();
                param.setResult(true);
            }
        });
    }

    private void hookBottomBar(Activity activity) throws Throwable {
        int idTabStrip = ContextUtils.getIdId("tab_strip");
        final ViewGroup rootView = (ViewGroup) activity.findViewById(android.R.id.content);
        List<View> views = ViewUtils.findAllViewById(rootView, idTabStrip);
        for (View view : views) {
            if (view instanceof ViewGroup) {
                ViewGroup tabStrip = (ViewGroup) view;
                Logger.d("tabStrip " + tabStrip);
                List<ViewGroup> layouts = ViewUtils.findAllViewByType(tabStrip, (Class<ViewGroup>) findClass(MAIN_TAB_ITEM));
                Logger.d("tabStrip with " + layouts.size() + " relative layouts.");
                if (layouts.size() == 0) {
                    continue;
                }

                SimpleDrawer.NavItem[] navItems = new SimpleDrawer.NavItem[layouts.size()];
                for (int i = 0; i < layouts.size(); ++i) {
                    ViewGroup layout = layouts.get(i);
                    ImageView imageView = ViewUtils.findViewByType(layout, ImageView.class);
                    if (imageView == null) {
                        continue;
                    }
                    TextView textView = ViewUtils.findViewByType(layout, TextView.class);
                    if (textView == null) {
                        continue;
                    }
                    SimpleDrawer.NavItem navItem = new SimpleDrawer.NavItem(imageView.getDrawable(), textView.getText(), layout);
                    navItems[i] = navItem;
                }
                SimpleDrawer.NavItem headerItem = new SimpleDrawer.NavItem(ContextUtils.getAppIcon(),
                        ContextUtils.getAppName(), null);

                FrameLayout allView = ViewUtils.rootChildsIntoOneLayout(activity);
                mDrawerLayout = new SimpleDrawer(activity, allView, navItems, headerItem);
                mDrawerLayout.updateHeaderBackground(Color.parseColor("#232326"));
                mDrawerLayout.updateNavListBackground(Color.parseColor("#232326"));
                mDrawerLayout.updateNavListTextColor(Color.WHITE);
                rootView.addView(mDrawerLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                tabStrip.setVisibility(View.GONE);
                Logger.i("Simple drawer is created.");
                break;
            }
        }
    }
}
