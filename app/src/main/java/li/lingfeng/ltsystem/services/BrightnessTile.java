package li.lingfeng.ltsystem.services;

import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.utils.Logger;

public class BrightnessTile extends TileService {

    @Override
    public IBinder onBind(Intent intent) {
        IBinder binder = super.onBind(intent);
        int value = Prefs.instance().getInt(R.string.key_quick_settings_tile_preconfigured_brightness, 0);
        if (value > 0) {
            Tile tile = getQsTile();
            tile.setLabel("Set " + value + " brightness");
            tile.updateTile();
        }
        return binder;
    }

    @Override
    public void onClick() {
        int value = Prefs.instance().getInt(R.string.key_quick_settings_tile_preconfigured_brightness, 0);
        if (value > 0) {
            Logger.i("Set brightness " + value);
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, value);
        }
    }
}
