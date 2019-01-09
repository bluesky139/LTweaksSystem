package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.content.Intent;
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

@MethodsLoad(packages = PackageNames.BILIBILI, prefs = R.string.key_bilibili_search)
public class BilibiliSearch extends TweakBase {

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(ClassNames.BILIBILI_SEARCH_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            Intent intent = activity.getIntent();
            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                String text = intent.getStringExtra("query");
                if (text != null) {
                    Logger.i("Search " + text);
                    EditText editText = (EditText) ViewUtils.findViewByName(activity, "search_src_text");
                    editText.setText(text);
                    editText.onEditorAction(EditorInfo.IME_ACTION_DONE);
                }
            }
        });
    }

    @Override
    public void android_app_Activity__onPause__(ILTweaks.MethodParam param) {
        afterOnClass(ClassNames.BILIBILI_SEARCH_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            if (!activity.isFinishing()) {
                Logger.d("Finish search activity.");
                activity.finish();
            }
        });
    }
}
