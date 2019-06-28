package li.lingfeng.ltsystem.tweaks.communication;

import android.text.StaticLayout;
import android.text.TextPaint;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.TELEGRAM, prefs = R.string.key_telegram_white_instant_view_text)
public class TelegramWhiteInstantViewText extends TweakBase {

    @Override
    public void android_text_StaticLayout__generate__Builder_boolean_boolean(ILTweaks.MethodParam param) {
        param.after(() -> {
            StaticLayout staticLayout = (StaticLayout) param.thisObject;
            TextPaint paint = staticLayout.getPaint();
            if (paint == null) {
                return;
            }
            int color = paint.getColor();
            if (color == 0xFF999999 || color == 0xFF666666) {
                Logger.v("Set StaticLayout text color 0xFFFAFAFA");
                paint.setColor(0xFFFAFAFA);
            }
        });
    }
}
