package li.lingfeng.ltsystem.tweaks.system;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = {}, prefs = {}, excludedPackages = PackageNames.ANDROID)
public class ToastBlock extends TweakBase {

    @Override
    public void android_widget_Toast__show__(ILTweaks.MethodParam param) {
        param.before(() -> {
            View nextView = ((Toast) param.thisObject).getView();
            TextView textView;
            if (nextView instanceof TextView) {
                textView = (TextView) nextView;
            } else {
                textView = ViewUtils.findViewByType((ViewGroup) nextView, TextView.class);
            }
            String text = textView.getText().toString();

            boolean block = false;
            List<String> words = Prefs.large().getStringList(R.string.key_display_toast_block_list, null);
            if (words != null) {
                for (String word : words) {
                    if (word.equals(text)) {
                        block = true;
                        break;
                    }
                }
            }
            Logger.v("[Toast] " + getPackageName() + ": " + text + (block ? " [blocked]" : ""));
            if (block) {
                param.setResult(null);
            }
        });
    }
}
