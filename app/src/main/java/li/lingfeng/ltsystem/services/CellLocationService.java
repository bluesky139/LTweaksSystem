package li.lingfeng.ltsystem.services;

import android.content.Context;
import android.content.Intent;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import li.lingfeng.ltsystem.prefs.NotificationId;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.services.base.ForegroundService;
import li.lingfeng.ltsystem.utils.Logger;

public class CellLocationService extends ForegroundService {

    private static final String CHANGE_ACTION = "li.lingfeng.ltsystem.LISTEN_CELL_LOCATION";

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i("CellLocationService onCreate.");
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(mListener, PhoneStateListener.LISTEN_CELL_LOCATION);
    }

    private PhoneStateListener mListener = new PhoneStateListener() {
        @Override
        public void onCellLocationChanged(CellLocation location) {
            if (location instanceof GsmCellLocation) {
                GsmCellLocation gsmCellLocation = (GsmCellLocation) location;
                Logger.v("Cell location change " + gsmCellLocation);
                broadcastToAutomate(gsmCellLocation);
            } else {
                Logger.w("Cell location change, but " + location.getClass().getSimpleName());
            }
        }
    };

    private void broadcastToAutomate(GsmCellLocation gsmCellLocation) {
        Intent intent = new Intent(CHANGE_ACTION);
        intent.putExtra("LAC", gsmCellLocation.getLac());
        intent.putExtra("CID", gsmCellLocation.getCid());
        intent.putExtra("PSC", gsmCellLocation.getPsc());
        intent.setPackage(PackageNames.AUTOMATE);
        sendBroadcast(intent);
    }

    @Override
    protected int getNotificationId() {
        return NotificationId.CELL_LOCATION_SERVICE;
    }
}
