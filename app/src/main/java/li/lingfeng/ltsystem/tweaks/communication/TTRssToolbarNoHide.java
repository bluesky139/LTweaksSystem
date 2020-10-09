package li.lingfeng.ltsystem.tweaks.communication;

import android.app.Activity;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.TT_RSS, prefs = R.string.key_ttrss_toolbar_no_hide)
public class TTRssToolbarNoHide extends TweakBase {

    private static final String MASTER_ACTIVITY = "org.fox.ttrss.MasterActivity";
    private static final String DETAIL_ACTIVITY = "org.fox.ttrss.DetailActivity";
    private static final int SCROLL_FLAG_SCROLL = 1;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(MASTER_ACTIVITY, param, () -> {
            setToolbarScroll(param);
        });
        afterOnClass(DETAIL_ACTIVITY, param, () -> {
            setToolbarScroll(param);
            setArticleHeaderScroll(param);
        });
    }

    private void setToolbarScroll(ILTweaks.MethodParam param) throws Throwable {
        final Activity activity = (Activity) param.thisObject;
        ViewGroup toolbar = ViewUtils.findViewByName(activity, "toolbar");
        ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
        ReflectUtils.callMethod(layoutParams, "setScrollFlags", new Object[] { 0 }, new Class[] { int.class });
    }

    private void setArticleHeaderScroll(ILTweaks.MethodParam param) throws Throwable {
        final Activity activity = (Activity) param.thisObject;
        ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boolean end = true;
                try {
                    ViewGroup toolbar = ViewUtils.findViewByName(activity, "article_header");
                    if (toolbar != null) {
                        ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
                        ReflectUtils.callMethod(layoutParams, "setScrollFlags", new Object[] { SCROLL_FLAG_SCROLL }, new Class[] { int.class });
                    } else {
                        end = false;
                    }
                } catch (Throwable e) {
                    Logger.e("setScrollFlags exception.", e);
                }
                if (end) {
                    rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

    }
}
