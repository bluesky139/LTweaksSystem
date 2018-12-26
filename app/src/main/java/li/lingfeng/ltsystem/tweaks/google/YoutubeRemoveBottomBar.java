package li.lingfeng.ltsystem.tweaks.google;

import android.app.Activity;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.ClassNames;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.SimpleDrawer;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.YOUTUBE, prefs = R.string.key_youtube_remove_bottom_bar)
public class YoutubeRemoveBottomBar extends TweakBase {

    private static final String MAIN_ACTIVITY = "com.google.android.apps.youtube.app.WatchWhileActivity";
    private SimpleDrawer mDrawerLayout;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        addHookOnActivity(MAIN_ACTIVITY, param, new ILTweaks.MethodHook() {
            @Override
            public void after() throws Throwable {
                final Activity activity = (Activity) param.thisObject;
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            hookBottomBar(activity);
                        } catch (Throwable e) {
                            Logger.e("hookBottomBar error.", e);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void android_app_Activity__onDestroy__(ILTweaks.MethodParam param) {
        addHookOnActivity(MAIN_ACTIVITY, param, new ILTweaks.MethodHook() {
            @Override
            public void after() throws Throwable {
                mDrawerLayout = null;
            }
        });
    }

    @Override
    public void android_app_Activity__onKeyUp__int_KeyEvent(ILTweaks.MethodParam param) {
        int keyCode = (int) param.args[0];
        KeyEvent event = (KeyEvent) param.args[1];
        if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking() && !event.isCanceled()) {
            param.addHook(new ILTweaks.MethodHook() {
                @Override
                public void before() throws Throwable {
                    if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                        Logger.i("Back is pressed for closing drawer.");
                        mDrawerLayout.closeDrawers();
                        param.setResult(true);
                    }
                }
            });
        }
    }

    private void hookBottomBar(final Activity activity) throws Throwable {
        int idPivotBar = ContextUtils.getIdId("bottom_bar_container");
        final ViewGroup pivotBar = (ViewGroup) activity.findViewById(idPivotBar);
        final Class clsConstraintLayout = findClass(ClassNames.CONSTRAINT_LAYOUT);
        List buttons = ViewUtils.findAllViewByTypeInSameHierarchy(pivotBar, clsConstraintLayout, 4);
        Logger.d("pivotBar with " + buttons.size() + " buttons.");

        SimpleDrawer.NavItem[] navItems = new SimpleDrawer.NavItem[buttons.size()];
        for (int i = 0; i < buttons.size(); ++i) {
            View button = (View) buttons.get(i);
            ImageView imageView = ViewUtils.findViewByType((ViewGroup) button, ImageView.class);
            TextView textView = ViewUtils.findViewByType((ViewGroup) button, TextView.class);
            SimpleDrawer.NavItem navItem = new SimpleDrawer.NavItem(imageView.getDrawable(), textView.getText(), button);
            navItems[i] = navItem;
        }
        SimpleDrawer.NavItem headerItem = new SimpleDrawer.NavItem(ContextUtils.getAppIcon(),
                ContextUtils.getAppName(), null);

        FrameLayout allView = ViewUtils.rootChildsIntoOneLayout(activity);
        mDrawerLayout = new SimpleDrawer(activity, allView, navItems, headerItem);
        int color = activity.getWindow().getStatusBarColor();
        mDrawerLayout.updateHeaderBackground(color);
        final ViewGroup rootView = (ViewGroup) activity.findViewById(android.R.id.content);
        rootView.addView(mDrawerLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        Logger.i("Simple drawer is created.");

        int idPaneContainer = ContextUtils.getIdId("pane_fragment_container");
        final View paneContainer = rootView.findViewById(idPaneContainer);
        updatePaneContainerHeight(pivotBar, paneContainer);
        pivotBar.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                Logger.i("pivotBar onLayoutChange.");
                try {
                    List buttons = ViewUtils.findAllViewByTypeInSameHierarchy(pivotBar, clsConstraintLayout, 4);
                    mDrawerLayout.updateClickObjs(buttons.toArray());
                    updatePaneContainerHeight(pivotBar, paneContainer);
                } catch (Throwable e) {
                    Logger.e("pivotBar onLayoutChange error, " + e);
                }
            }
        });
    }

    private void updatePaneContainerHeight(View pivotBar, View paneContainer) {
        if (paneContainer.getLayoutParams().height > 0) {
            return;
        }

        int pivotBarHeight = pivotBar.getMeasuredHeight();
        int oldPaneHeight = paneContainer.getMeasuredHeight();
        if (pivotBarHeight == 0 || oldPaneHeight == 0) {
            return;
        }

        int newPaneHeight = oldPaneHeight + pivotBarHeight;
        paneContainer.getLayoutParams().height = newPaneHeight;
        pivotBar.getLayoutParams().height = 0;
        Logger.d("pivotBarHeight " + pivotBarHeight + ", paneContainer new height " + newPaneHeight);
    }
}
