package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.util.LruCache;

import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.COMIC_SCREEN, prefs = R.string.key_comic_screen_no_zip_cache, hiddenApiExemptions = {
        "Ljava/io/FileOutputStream;", "Ljava/io/FileInputStream;"
})
public class ComicScreenNoZipCache extends TweakBase {

    private static final String BUFFERED_OUTPUT_STREAM = BufferedOutputStream.class.getName();
    private static final String BUFFERED_INPUT_STREAM = BufferedInputStream.class.getName();
    private static final String COMIC_LIST_ACTIVITY = "com.viewer.comicscreen.ListActivity";

    private String mUnzipPath;
    private LruCache<String, SharedBytesOutputStream> mMemoryStreams = new LruCache<String, SharedBytesOutputStream>(60) {
        @Override
        protected void entryRemoved(boolean evicted, String key, SharedBytesOutputStream oldValue, SharedBytesOutputStream newValue) {
            //Logger.d("entryRemoved " + key);
            FileUtils.deleteQuietly(new File(key));
        }
    };

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        beforeOnClass(COMIC_LIST_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            mUnzipPath = activity.getCacheDir().getPath() + "/unzip";
            File file = new File(mUnzipPath);
            if (file.exists()) {
                Logger.v("Clean unzip dir.");
                FileUtils.cleanDirectory(file);
            }
        });
    }

    private class SharedBytesOutputStream extends ByteArrayOutputStream {

        private String mPath;

        public SharedBytesOutputStream(String path) {
            super(3 * 512 * 1024);
            mPath = path;
        }

        @Override
        public void close() throws IOException {
            super.close();
            //Logger.d("SharedBytesOutputStream close " + mPath);
            RandomAccessFile file = new RandomAccessFile(mPath, "rw");
            file.setLength(size());
            file.close();
        }

        public ByteArrayInputStream toInputStream() {
            return new ByteArrayInputStream(buf, 0, count);
        }
    }

    @Override
    public void java_io_FilterOutputStream__FilterOutputStream__OutputStream(ILTweaks.MethodParam param) {
        beforeOnClass(BUFFERED_OUTPUT_STREAM, param, () -> {
            if (mUnzipPath == null) {
                return;
            }
            if (param.args[0] instanceof FileOutputStream) {
                FileOutputStream outputStream = (FileOutputStream) param.args[0];
                String path = (String) ReflectUtils.getObjectField(outputStream, "path");
                if (path != null && path.startsWith(mUnzipPath)) {
                    Logger.v("Output file " + path);
                    SharedBytesOutputStream memoryStream = new SharedBytesOutputStream(path);
                    mMemoryStreams.put(path, memoryStream);
                    param.setArg(0, memoryStream);
                    outputStream.close();
                }
            }
        });
    }

    @Override
    public void java_io_FilterInputStream__FilterInputStream__InputStream(ILTweaks.MethodParam param) {
        beforeOnClass(BUFFERED_INPUT_STREAM, param, () -> {
            if (mUnzipPath == null) {
                return;
            }
            if (param.args[0] instanceof FileInputStream) {
                FileInputStream inputStream = (FileInputStream) param.args[0];
                String path = (String) ReflectUtils.getObjectField(inputStream, "path");
                if (path != null && path.startsWith(mUnzipPath)) {
                    SharedBytesOutputStream outputStream = mMemoryStreams.get(path);
                    if (outputStream == null) {
                        Logger.e("Null stream from memory streams, " + path);
                    } else {
                        ByteArrayInputStream memoryStream = outputStream.toInputStream();
                        //Logger.v("Input file " + path + ", size " + memoryStream.available());
                        param.setArg(0, memoryStream);
                        inputStream.close();
                    }
                }
            }
        });
    }
}
