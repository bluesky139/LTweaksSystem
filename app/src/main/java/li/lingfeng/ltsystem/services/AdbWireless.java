package li.lingfeng.ltsystem.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.net.wifi.WifiManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.v4.app.NotificationCompat;
import android.text.format.Formatter;
import android.widget.Toast;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.prefs.NotificationId;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.Shell;

public class AdbWireless extends TileService {

    private static final String ADB_WIRELESS_OFF = AdbWireless.class + ".ADB_WIRELESS_OFF";
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

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onClick();
            }
        }, new IntentFilter(ADB_WIRELESS_OFF));
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
                setupNotification(AdbWireless.this, isOn);
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

    private void setupNotification(Context context, boolean isOn) {
        try {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (isOn) {
                Intent intent = new Intent(ADB_WIRELESS_OFF);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                String title = getString(R.string.adb_wireless_notification_title);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_quick_settings_adb_wireless_on)
                        .setWhen(0)
                        .setOngoing(true)
                        .setTicker("Adb Wireless")
                        .setDefaults(0)
                        .setPriority(Notification.PRIORITY_LOW)
                        .setContentTitle(title)
                        .setContentText(getString(R.string.adb_wireless_notification_text))
                        .setContentIntent(pendingIntent)
                        .setChannelId(title)
                        .setVisibility(Notification.VISIBILITY_PUBLIC);
                notificationManager.createNotificationChannel(
                        new NotificationChannel(title, title, NotificationManager.IMPORTANCE_LOW));
                Notification notification = builder.build();
                notificationManager.notify(NotificationId.ADB_WIRELESS, notification);
            } else {
                notificationManager.cancel(NotificationId.ADB_WIRELESS);
            }
        } catch (Throwable e) {
            Logger.e("Can't set notification for " + getClass().getSimpleName() + ", " + e);
        }
    }
}
