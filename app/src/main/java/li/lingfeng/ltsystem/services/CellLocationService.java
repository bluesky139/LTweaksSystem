package li.lingfeng.ltsystem.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import java.util.List;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.prefs.NotificationId;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.services.base.ForegroundService;
import li.lingfeng.ltsystem.utils.Logger;

public class CellLocationService extends ForegroundService {

    private static final String LISTEN_CELL_LOCATION = "li.lingfeng.ltsystem.LISTEN_CELL_LOCATION";
    private static final String LISTEN_HOME_CELLS = "li.lingfeng.ltsystem.LISTEN_HOME_CELLS";
    private TelephonyManager mTelephonyManager;
    private boolean mAtHome = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_CELL_LOCATION);
    }

    private PhoneStateListener mListener = new PhoneStateListener() {
        @Override
        public void onCellLocationChanged(CellLocation location) {
            if (location instanceof GsmCellLocation) {
                GsmCellLocation gsmCellLocation = (GsmCellLocation) location;
                Logger.v("Cell location change " + gsmCellLocation);
                if (Prefs.instance().getBoolean(R.string.key_phone_broadcast_cell_location_change, false)) {
                    broadcastToAutomate(gsmCellLocation);
                }
                if (Prefs.instance().getBoolean(R.string.key_phone_record_cells, false)) {
                    recordCells(gsmCellLocation);
                }
                if (Prefs.instance().getBoolean(R.string.key_phone_broadcast_home_cells, false)) {
                    broadcastHomeCells(gsmCellLocation);
                }
            } else {
                Logger.w("Cell location change, but " + location.getClass().getSimpleName());
            }
        }
    };

    private void broadcastToAutomate(GsmCellLocation gsmCellLocation) {
        Intent intent = new Intent(LISTEN_CELL_LOCATION);
        intent.putExtra("LAC", gsmCellLocation.getLac());
        intent.putExtra("CID", gsmCellLocation.getCid());
        intent.putExtra("PSC", gsmCellLocation.getPsc());
        intent.setPackage(PackageNames.AUTOMATE);
        sendBroadcast(intent);
    }

    private void recordCells(GsmCellLocation gsmCellLocation) {
        String cell = System.currentTimeMillis() / 1000 + "-"
                + getNetworkClass() + ":" + gsmCellLocation.getLac() + ":" + gsmCellLocation.getCid();
        Prefs.large().appendStringToList(R.string.key_phone_cells, cell, 15);
    }

    @SuppressLint("MissingPermission")
    private String getNetworkClass() {
        int networkType = mTelephonyManager.getVoiceNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GSM:
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            case TelephonyManager.NETWORK_TYPE_NR:
                return "5G";
            default:
                return "Unknown";
        }
    }

    private void broadcastHomeCells(GsmCellLocation gsmCellLocation) {
        List<String> homeCells = Prefs.large().getStringList(R.string.key_phone_home_cells, null);
        String cell = getNetworkClass() + ":" + gsmCellLocation.getLac() + ":" + gsmCellLocation.getCid();
        boolean atHome = homeCells.contains(cell);
        if (atHome != mAtHome) {
            Logger.i("mAtHome " + mAtHome + " -> " + atHome);
            mAtHome = atHome;
            Intent intent = new Intent(LISTEN_HOME_CELLS);
            intent.putExtra("at_home", atHome);
            intent.setPackage(PackageNames.AUTOMATE);
            sendBroadcast(intent);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Prefs.instance().getBoolean(R.string.key_phone_broadcast_cell_location_change, false)
                || Prefs.instance().getBoolean(R.string.key_phone_record_cells, false)
                || Prefs.instance().getBoolean(R.string.key_phone_broadcast_home_cells, false)) {
            intent.removeExtra("stop");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected int getNotificationId() {
        return NotificationId.CELL_LOCATION_SERVICE;
    }
}
