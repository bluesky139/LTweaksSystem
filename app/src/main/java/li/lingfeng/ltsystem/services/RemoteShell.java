package li.lingfeng.ltsystem.services;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import li.lingfeng.ltsystem.LTHelper;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.Shell;

public class RemoteShell extends ContentProvider {

    public static void su(String cmd) {
        ContentValues values = new ContentValues();
        values.put("cmd", cmd);
        LTHelper.currentApplication().getContentResolver()
                .insert(Uri.parse("content://li.lingfeng.ltsystem.remoteShell/"), values);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (Binder.getCallingUid() != 1000) {
            return null;
        }
        String cmd = values.getAsString("cmd");
        Logger.d("RemoteShell " + cmd);
        Shell.su(cmd);
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
