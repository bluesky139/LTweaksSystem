package li.lingfeng.ltsystem.tweaks.system;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.WeakHashMap;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.LTHelper;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.MIXPLORER, prefs = R.string.key_mixplorer_highlight_visited_files)
public class MiXplorerHighlightVisitedFiles extends TweakBase {

    private static final String BROWSER_ACTIVITY = "com.mixplorer.activities.BrowseActivity";
    private static final String MI_PAGER = "com.mixplorer.widgets.MiPager";
    private static final int HIGHLIGHT_COLOR = 0xFF383838;
    private DBHelper mDBHelper;
    private String mNavPath;
    private String mVisitedFile;
    private WeakHashMap<ViewGroup, Void> mPages = new WeakHashMap<>(5);

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(BROWSER_ACTIVITY, param, () -> {
            if (mDBHelper == null) {
                mDBHelper = new DBHelper();
            }
            Activity activity = (Activity) param.thisObject;
            ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();

            TextView navPath = (TextView) ViewUtils.findViewByName(rootView, "nav_path");
            navPath.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    mNavPath = s.toString();
                    mVisitedFile = mDBHelper.getVisitedFile(mNavPath);
                    Logger.v("Nav path changed " + mNavPath + ", visited file " + mVisitedFile);
                    try {
                        refreshHightlight();
                    } catch (Throwable e) {
                        Logger.e("refreshHightlight exception after nav path changed.", e);
                    }
                }
            });

            ViewGroup miPager = (ViewGroup) ViewUtils.findViewByType(rootView, findClass(MI_PAGER));
            for (int i = 0; i < miPager.getChildCount(); ++i) {
                ViewGroup child = (ViewGroup) miPager.getChildAt(i);
                pageAdded(child);
            }

            miPager.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                @Override
                public void onChildViewAdded(View parent, View child) {
                    pageAdded((ViewGroup) child);
                }

                @Override
                public void onChildViewRemoved(View parent, View child) {
                    Logger.d("MiPager onChildViewRemoved " + child);
                    ViewGroup page = (ViewGroup) ((ViewGroup) child).getChildAt(0);
                    Logger.v("Page removed " + page);
                    page.setOnHierarchyChangeListener(null);
                    mPages.remove(page);
                }
            });
        });
    }

    private void pageAdded(ViewGroup pagerChild) {
        Logger.d("MiPager pageAdded pagerChild " + pagerChild);
        ViewGroup page = (ViewGroup) pagerChild.getChildAt(0);
        if (mPages.containsKey(page)) {
            return;
        }
        Logger.v("Page added " + page);

        for (int i = 0; i < page.getChildCount(); ++i) {
            fileAdded(page.getChildAt(i));
        }
        page.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                fileAdded(child);
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
                //Logger.v("File removed " + child);
                child.setBackground(null);
            }
        });
        mPages.put(page, null);
    }

    private void fileAdded(View fileChild) {
        //Logger.d("File added " + fileChild);
        try {
            String name = getFileNameIfItsFile(fileChild);
            //Logger.v("File name: " + name);
            if (name != null && name.equals(mVisitedFile)) {
                Logger.i("It's visited file: " + mVisitedFile);
                fileChild.setBackgroundColor(HIGHLIGHT_COLOR);
            } else {
                fileChild.setBackground(null);
            }

            View.OnClickListener originalListener = ViewUtils.getViewClickListener(fileChild);
            if (originalListener.getClass().getName().startsWith(PackageNames.L_TWEAKS)) {
                return;
            }
            fileChild.setOnClickListener((v) -> {
                try {
                    String name2 = getFileNameIfItsFile(v);
                    if (name2 != null) {
                        Logger.i("File visited: " + name2);
                        mVisitedFile = name2;
                        mDBHelper.fileVisited(mNavPath, name2);
                        ViewGroup page = (ViewGroup) v.getParent();
                        for (int i = 0; i < page.getChildCount(); ++i) {
                            page.getChildAt(i).setBackground(null);
                        }
                        v.setBackgroundColor(HIGHLIGHT_COLOR);
                    }
                } catch (Throwable e) {
                    Logger.e("File click exception.", e);
                }
                originalListener.onClick(v);
            });
        } catch (Throwable e) {
            Logger.e("File added exception.", e);
        }
    }

    private void refreshHightlight() throws Throwable {
        ViewGroup page = null;
        for (Map.Entry<ViewGroup, Void> entry : mPages.entrySet()) {
            ViewGroup _page = entry.getKey();
            int[] location = new int[2];
            _page.getLocationInWindow(location);
            if (_page.getVisibility() == View.VISIBLE && location[0] == 0) {
                page = _page;
                break;
            }
        }

        for (int i = 0; i < page.getChildCount(); ++i) {
            View child = page.getChildAt(i);
            String name = getFileNameIfItsFile(child);
            if (name != null) {
                if (name.equals(mVisitedFile)) {
                    child.setBackgroundColor(HIGHLIGHT_COLOR);
                } else {
                    child.setBackground(null);
                }
            }
        }
    }

    private String getFileNameIfItsFile(View fileChild) throws Throwable {
        String desc = (String) ReflectUtils.callMethod(fileChild, "getContentDescription");
        String[] strings = StringUtils.split(desc, '\n');
        return strings[1].endsWith("B") ? strings[0] : null;
    }

    class DBHelper extends SQLiteOpenHelper {

        private LruCache<String, String> cache = new LruCache<>(20);

        public DBHelper() {
            super(LTHelper.currentApplication(), "ltweaks_highlight_visited_files", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = "CREATE TABLE IF NOT EXISTS Visited (" +
                    "Folder TEXT PRIMARY KEY NOT NULL, " +
                    "File TEXT NOT NULL" +
                    ");";
            execSQL(db, sql);
            sql = "CREATE UNIQUE INDEX IF NOT EXISTS VisitedIndex ON Visited (Folder);";
            execSQL(db, sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        private void execSQL(SQLiteDatabase db, String sql) {
            Logger.d("execSQL: " + sql);
            db.execSQL(sql);
        }

        private Cursor rawQuery(SQLiteDatabase db, String sql) {
            //Logger.d("rawQuery: " + sql);
            return db.rawQuery(sql, null);
        }

        public void fileVisited(String folder, String file) {
            String sql = "REPLACE INTO Visited (Folder, File) VALUES ('%1$s', '%2$s');";
            execSQL(getWritableDatabase(), String.format(sql, folder, file));
            cache.put(folder, file);
        }

        public String getVisitedFile(String folder) {
            String file = cache.get(folder);
            if (file != null) {
                return !file.isEmpty() ? file : null;
            }
            String sql = "SELECT File from Visited WHERE Folder='%1$s' LIMIT 1;";
            Cursor cursor = rawQuery(getWritableDatabase(), String.format(sql, folder));
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                file = cursor.getString(0);
            }
            cursor.close();
            cache.put(folder, file != null ? file : "");
            return file;
        }
    }
}
