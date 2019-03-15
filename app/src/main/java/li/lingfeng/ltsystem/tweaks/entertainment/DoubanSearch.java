package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.os.Handler;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.ClassNames;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.DOUBAN, prefs = R.string.key_douban_search)
public class DoubanSearch extends TweakBase {

    private boolean mPendingIMEEvent = false;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(ClassNames.DOUBAN_SEARCH_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            if (activity.getIntent().getBooleanExtra("from_ltweaks", false)) {
                mPendingIMEEvent = true;
                activity.getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        try {
                            EditText editText = (EditText) ViewUtils.findViewByName(activity, "search");
                            if (!editText.getText().toString().isEmpty()) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Logger.i("Send IME search event.");
                                        editText.onEditorAction(EditorInfo.IME_ACTION_SEARCH);
                                        mPendingIMEEvent = false;
                                    }
                                }, 500);
                                activity.getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            }
                        } catch (Throwable e) {
                            Logger.e("Send IME search event exception.", e);
                            mPendingIMEEvent = false;
                            activity.getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                });
            } else {
                mPendingIMEEvent = false;
            }
        });
    }

    @Override
    public void android_view_inputmethod_InputMethodManager__showSoftInput__View_int_ResultReceiver(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (mPendingIMEEvent) {
                Logger.v("Disallow showSoftInput due to IME pending event.");
                param.setResult(false);
            }
        });
    }
}
