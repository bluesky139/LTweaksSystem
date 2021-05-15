package li.lingfeng.ltsystem.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.utils.IOUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ShareUtils;

public class ImageShareRedirectActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!getIntent().getAction().equals(Intent.ACTION_SEND)
                || !getIntent().getType().startsWith("image/")) {
            Toast.makeText(this, R.string.not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
        Logger.d("uri " + uri);
        if (uri == null) {
            Toast.makeText(this, R.string.not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        byte[] bytes = IOUtils.uri2bytes(uri);
        try {
            FileUtils.writeByteArrayToFile(new File("/sdcard/Tencent/ltweaks_share_image.png"), bytes);
            ShareUtils.shareImage(this, "/sdcard/Tencent/ltweaks_share_image.png");
        } catch (IOException e) {
            Logger.e("Failed to redirect share image.", e);
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
