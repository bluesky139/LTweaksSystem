package li.lingfeng.ltsystem;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.PermissionUtils;
import li.lingfeng.ltsystem.utils.Shell;
import li.lingfeng.ltsystem.utils.ViewUtils;

public class MainActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.FLAVOR.equals("selfUse")) {
            setTitle(getTitle() + " - Self Use");
        }
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
        if (BuildConfig.FLAVOR.equals("selfUse")) {
            loadHeadersFromResource(ContextUtils.getXmlId("pref_headers_self_use"), target);
        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return fragmentName.startsWith(PackageNames.L_TWEAKS + ".fragments.");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_bg_dexopt:
                ViewUtils.showDialog(this, R.string.app_bg_dexopt, (dialog, which) -> {
                    NotificationManager notificationManager = (NotificationManager) LTHelper.currentApplication().getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(LTHelper.currentApplication())
                            .setSmallIcon(R.mipmap.ic_launcher_round)
                            .setWhen(0)
                            .setOngoing(true)
                            .setTicker("Running dexopt...")
                            .setDefaults(0)
                            .setPriority(Notification.PRIORITY_LOW)
                            .setContentTitle("LTweaks dexopt")
                            .setContentText("Runing dexopt...")
                            .setChannelId("LTweaks dexopt")
                            .setVisibility(Notification.VISIBILITY_PUBLIC)
                            .setProgress(0, 0, true);
                    notificationManager.createNotificationChannel(
                            new NotificationChannel("LTweaks dexopt", "LTweaks dexopt", NotificationManager.IMPORTANCE_LOW));
                    Notification notification = builder.build();
                    notificationManager.notify(100, notification);

                    long startTime = System.currentTimeMillis();
                    new Shell("su", new String[] {
                            "cmd package bg-dexopt-job"
                    }, 0, (isOk, stderr, stdout) -> {
                        notificationManager.cancel(100);
                        int cost = (int) ((System.currentTimeMillis() - startTime) / 1000);
                        Logger.d("cmd package bg-dexopt-job return " + isOk + ", cost " + cost + "s.");
                        ViewUtils.showDialog(MainActivity.this, "dexopt end, please reboot.\nTime cost: " + cost / 60 + "m");
                    }).execute();
                });
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}
