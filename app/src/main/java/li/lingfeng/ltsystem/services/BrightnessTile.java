package li.lingfeng.ltsystem.services;

import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.utils.Logger;

public class BrightnessTile extends TileService {

    @Override
    public void onStartListening() {
        int value = Prefs.instance().getInt(R.string.key_quick_settings_tile_preconfigured_brightness, 0);
        if (value > 0) {
            updateTile(value);
        }
    }

    @Override
    public void onClick() {
        int value = Prefs.instance().getInt(R.string.key_quick_settings_tile_preconfigured_brightness, 0);
        if (value > 0) {
            Logger.i("Set brightness " + value);
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, value);
            updateTile(value);
        }
    }

    private void updateTile(int value) {
        Tile tile = getQsTile();
        tile.setLabel("Set " + value + " brightness");
        try {
            tile.setState(Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE)
                    == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        } catch (Settings.SettingNotFoundException e) {
            tile.setState(Tile.STATE_UNAVAILABLE);
        }
        tile.updateTile();
    }
}
