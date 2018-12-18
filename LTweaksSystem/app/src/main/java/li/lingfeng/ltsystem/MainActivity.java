package li.lingfeng.ltsystem;

import android.os.SystemProperties;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.File;

import li.lingfeng.ltsystem.utils.Logger;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File file = new File("/data/ltweaks/aa");
        Log.d("LTweaks", "read " + file.canRead() + ", write " + file.canWrite());
        Logger.d("test SystemProperty " + SystemProperties.get("persist.ltweaks.apk_path"));
    }
}
