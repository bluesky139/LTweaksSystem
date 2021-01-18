package li.lingfeng.ltsystem.tweaks.shopping;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.activities.JDHistoryLayout;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.JD, prefs = R.string.key_jd_history)
public class JDHistory extends TweakBase {

    private static final String DETAIL_ACTIVITY = "com.jd.lib.productdetail.ProductDetailActivity";

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(DETAIL_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            View shareView = ViewUtils.findViewByName(activity, "pd_nav_share");
            shareView.setOnLongClickListener(view -> {
                showHistoryDialog(activity);
                return true;
            });
        });
    }

    private void showHistoryDialog(Activity activity) {
        new AlertDialog.Builder(activity)
                .setView(new JDHistoryLayout(activity))
                .show();
    }
}
