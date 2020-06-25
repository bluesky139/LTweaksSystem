package li.lingfeng.ltsystem.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.utils.Logger;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.i("Boot completed.");
        if (Prefs.instance().getBoolean(R.string.key_system_share_copy_to_share, false)) {
            intent = new Intent(context, CopyToShareService.class);
            context.startService(intent);
        }
        if (Prefs.instance().getBoolean(R.string.key_phone_broadcast_cell_location_change, false)) {
            intent = new Intent(context, CellLocationService.class);
            context.startService(intent);
        }
    }
}
