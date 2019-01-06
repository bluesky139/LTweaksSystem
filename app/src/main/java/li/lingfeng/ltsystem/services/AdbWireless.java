package li.lingfeng.ltsystem.services;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.net.wifi.WifiManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.text.format.Formatter;
import android.widget.Toast;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.Shell;

public class AdbWireless extends TileService {

    private boolean mSetToInactiveFirst = false;

    @Override
    public void onStartListening() {
        if (mSetToInactiveFirst) {
            return;
        }
        mSetToInactiveFirst = true;
        Tile tile = getQsTile();
        tile.setState(Tile.STATE_INACTIVE);
        tile.updateTile();
    }

    @Override
    public void onClick() {
        boolean isOn = getQsTile().getState() != Tile.STATE_ACTIVE;
        new Shell("su", new String[] {
                "setprop service.adb.tcp.port " + (isOn ? "5555" : "-1"),
                "stop adbd",
                "start adbd"
        },
                3000, (isOk, stderr, stdout) -> {
            Logger.d("Adb Wireless onResult " + isOk);
            if (isOk) {
                Toast.makeText(AdbWireless.this, isOn ? "Switched to adb wireless" : "Switched to adb usb", Toast.LENGTH_LONG).show();
                Tile tile = getQsTile();
                tile.setIcon(Icon.createWithResource(AdbWireless.this,
                        isOn ? R.drawable.ic_quick_settings_adb_wireless_on : R.drawable.ic_quick_settings_adb_wireless_off));
                tile.setLabel(getTileName(AdbWireless.this, isOn));
                tile.setState(isOn ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
                tile.updateTile();
            } else {
                Toast.makeText(AdbWireless.this, "Failed to switch adb, no root?", Toast.LENGTH_LONG).show();
            }
        }).execute();
    }

    private String getTileName(Context context, boolean isOn) {
        if (isOn) {
            try {
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                int ip = wifiManager.getConnectionInfo().getIpAddress();
                String strIp = Formatter.formatIpAddress(ip);
                Logger.d("Got ip " + strIp);
                return strIp;
            } catch (Throwable e) {
                Logger.e("Can't get ip, " + e);
            }
        }
        return "Adb Wireless";
    }
}
