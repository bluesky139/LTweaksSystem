package li.lingfeng.ltsystem.tweaks.system;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_display_light_amber_in_daylight)
public class LightAmberInDaylight extends TweakBase {

    private static final String NIGHT_DISPLAY_SERVICE = "com.android.server.display.ColorDisplayService";
    private static final float[] MATRIX_LIGHT_AMBER = new float[] { // ~= 5500K
            1,      0,      0, 0,
            0, 0.933f,      0, 0,
            0,      0, 0.870f, 0,
            0,      0,      0, 1
    };

    @Override
    public void com_android_server_display_color_ColorDisplayService__static__(ILTweaks.MethodParam param) {
        param.after(() -> {
            Logger.i("Set light amber in daylight.");
            ReflectUtils.setStaticObjectField(findClass(NIGHT_DISPLAY_SERVICE), "MATRIX_IDENTITY", MATRIX_LIGHT_AMBER);
        });
    }
}
