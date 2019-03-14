package li.lingfeng.ltsystem.tweaks.communication;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

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

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(MASTER_ACTIVITY, param, () -> {
            final Activity activity = (Activity) param.thisObject;
            ViewGroup toolbar = (ViewGroup) ViewUtils.findViewByName(activity, "toolbar");
            TextView textView = (TextView) toolbar.getChildAt(0);
            textView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    Logger.v("Toolbar afterTextChanged " + s.toString());
                    try {
                        Object fragmentManager = ReflectUtils.callMethod(activity, "getSupportFragmentManager");
                        Object fragment = ReflectUtils.callMethod(fragmentManager, "findFragmentByTag", "headlines");
                        ReflectUtils.setIntField(fragment, "m_scrollToToggleBar", Integer.MAX_VALUE);
                    } catch (Throwable e) {
                        Logger.e("afterTextChanged exception.", e);
                    }
                }
            });
        });

        afterOnClass(DETAIL_ACTIVITY, param, () -> {
            final Activity activity = (Activity) param.thisObject;
            activity.getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    try {
                        View scrollView = ViewUtils.findViewByName(activity, "article_scrollview");
                        if (scrollView != null) {
                            ReflectUtils.setObjectField(scrollView, "mOnScrollChangedListener", null);
                            activity.getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    } catch (Throwable e) {
                        Logger.e("Remove article_scrollview mOnScrollChangedListener exception.", e);
                    }
                }
            });
        });
    }
}
