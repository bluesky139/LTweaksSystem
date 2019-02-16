package li.lingfeng.ltsystem.services;

import android.content.Intent;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.utils.Logger;

public class Switch4G3G extends TileService {

    private boolean mIs4G = true;

    @Override
    public void onClick() {
        mIs4G = !mIs4G;
        Logger.d("Switch4G3G onClick " + mIs4G);
        Tile tile = getQsTile();
        Intent intent = new Intent(li.lingfeng.ltsystem.tweaks.system.Switch4G3G.ACTION_SWITCH);
        intent.putExtra("is_on", mIs4G);
        sendBroadcast(intent);

        tile.setIcon(Icon.createWithResource(this, mIs4G ? R.drawable.ic_4g : R.drawable.ic_3g));
        tile.updateTile();
    }
}
