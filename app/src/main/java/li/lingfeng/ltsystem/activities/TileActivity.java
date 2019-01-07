package li.lingfeng.ltsystem.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;

import li.lingfeng.ltsystem.prefs.ClassNames;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.services.AdbWireless;
import li.lingfeng.ltsystem.services.BrightnessTile;
import li.lingfeng.ltsystem.services.Switch4G3G;
import li.lingfeng.ltsystem.utils.Logger;

public class TileActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ComponentName componentName = getIntent().getParcelableExtra(Intent.EXTRA_COMPONENT_NAME);
        String clsName = componentName.getClassName();
        Logger.d(clsName + " long click.");
        if (clsName.equals(AdbWireless.class.getName())) {
            adbWirelessLongClick();
        } else if (clsName.equals(Switch4G3G.class.getName())) {
            switch4G3GLongClick();
        } else if (clsName.equals(BrightnessTile.class.getName())) {
            brightnessTileLongClick();
        }
        finish();
    }

    private void adbWirelessLongClick() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void switch4G3GLongClick() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName(PackageNames.ANDROID_SETTINGS, ClassNames.RADIO_INFO));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void brightnessTileLongClick() {
        Logger.i("Set auto brightness.");
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }
}
