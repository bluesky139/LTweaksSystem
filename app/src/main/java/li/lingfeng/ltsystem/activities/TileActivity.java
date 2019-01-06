package li.lingfeng.ltsystem.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import li.lingfeng.ltsystem.services.AdbWireless;
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
        }
        finish();
    }

    private void adbWirelessLongClick() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
