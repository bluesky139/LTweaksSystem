package li.lingfeng.ltsystem.services;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.NotImplementedException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;

/**
 * Created by lilingfeng on 2018/1/22.
 */

public class ResourceProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public AssetFileDescriptor openAssetFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        if (!"r".equals(mode)) {
            throw new UnsupportedOperationException("ResourceProvider mode " + mode + " is not supported.");
        }
        List<String> pathSegments = uri.getPathSegments();
        String type = pathSegments.get(0);
        String name = pathSegments.get(1);
        if (pathSegments.size() > 2) {
            name = name + "/" + pathSegments.get(2);
        }
        if (type.equals("raw")) {
            int rawId = ContextUtils.getRawId(name);
            return getContext().getResources().openRawResourceFd(rawId);
        } else if (type.equals("assets")) {
            try {
                return getContext().getAssets().openFd(name);
            } catch (IOException e) {
                Logger.e("openAssetFile exception.", e);
                throw new RuntimeException(e);
            }
        } else {
            throw new NotImplementedException("ResourceProvider openFile type " + type);
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        throw new NotImplementedException("ResourceProvider query");
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new NotImplementedException("ResourceProvider getType");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new UnsupportedOperationException("ResourceProvider insert is not supported.");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("ResourceProvider delete is not supported.");
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("ResourceProvider update is not supported.");
    }
}
