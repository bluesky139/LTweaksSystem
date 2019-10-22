package li.lingfeng.ltsystem.tweaks.system;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.util.List;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.LTHelper;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.SOLID_EXPLORER, prefs = R.string.key_solid_explorer_highlight_visited_files)
public class SolidExplorerHighlightVisitedFile extends TweakBase {

    private static final String MAIN_ACTIVITY = "pl.solidexplorer.SolidExplorer";
    private static final String SAFE_SWIPE_REFRESH_LAYOUT = "pl.solidexplorer.common.gui.SafeSwipeRefreshLayout";
    private static final String CHECKABLE_RELATIVE_LAYOUT = "pl.solidexplorer.common.gui.CheckableRelativeLayout";

    private Handler mHandler;
    private List<View> mHeaderViews;
    private DBHelper mDBHelper;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(MAIN_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();

            mHandler = new Handler();
            mDBHelper = new DBHelper();
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    try {
                        List<View> views = ViewUtils.findAllViewByType(rootView, (Class<View>) findClass(SAFE_SWIPE_REFRESH_LAYOUT));
                        mHeaderViews = ViewUtils.findAllViewByName(rootView, "smart_header");
                        if (views.size() == 2 && mHeaderViews.size() == 2) {
                            Logger.d("2 list.");
                            rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            for (int i = 0; i < 2; ++i) {
                                GridView gridView = views.get(i).findViewById(android.R.id.list);
                                AdapterView.OnItemClickListener originalItemClickListener = gridView.getOnItemClickListener();
                                gridView.setOnItemClickListener((parent, view, position, id) -> {
                                    originalItemClickListener.onItemClick(parent, view, position, id);
                                    fileClicked(view);
                                });

                                gridView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                                    @Override
                                    public void onChildViewAdded(View parent, View child) {
                                        try {
                                            if (child.getClass().getName().equals(CHECKABLE_RELATIVE_LAYOUT)) {
                                                TextView titleView = (TextView) ViewUtils.findViewByName((ViewGroup) child, "title");
                                                checkTitle(titleView);

                                                titleView.addTextChangedListener(new TextWatcher() {
                                                    @Override
                                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                                    }

                                                    @Override
                                                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                    }

                                                    @Override
                                                    public void afterTextChanged(Editable s) {
                                                        checkTitle(titleView);
                                                    }
                                                });
                                            }
                                        } catch (Throwable e) {
                                            Logger.e("listView onChildViewAdded exception.", e);
                                        }
                                    }

                                    @Override
                                    public void onChildViewRemoved(View parent, View child) {
                                    }
                                });
                            }
                        }
                    } catch (Throwable e) {
                        Logger.e("onGlobalLayout exception.", e);
                    }
                }
            });
        });
    }

    @Override
    public void android_app_Activity__onDestroy__(ILTweaks.MethodParam param) {
        param.before(() -> {
            mHandler = null;
            mHeaderViews = null;
            mDBHelper = null;
        });
    }

    private void checkTitle(TextView titleView) {
        mHandler.post(() -> {
            try {
                ViewGroup parent = (ViewGroup) titleView.getParent();
                TextView subtitleView = (TextView) ViewUtils.findViewByName(parent, "subtitle1");
                String subtitle = subtitleView.getText().toString();
                if (subtitle.endsWith("B") && !subtitle.contains("/")) {
                    String title = titleView.getText().toString();
                    String folder = getCurrentFolder();
                    if (title.equals(mDBHelper.getVisitedFile(folder))) {
                        Logger.v("Visited file " + folder + "/" + title);
                        parent.setBackgroundColor(Color.DKGRAY);
                    } else {
                        parent.setBackground(null);
                    }
                } else {
                    parent.setBackground(null);
                }
            } catch (Throwable e) {
                Logger.e("Check title exception.", e);
            }
        });
    }

    private void fileClicked(View view) {
        try {
            if (view.getClass().getName().equals(CHECKABLE_RELATIVE_LAYOUT)) {
                TextView titleView = (TextView) ViewUtils.findViewByName((ViewGroup) view, "title");
                TextView subtitleView = (TextView) ViewUtils.findViewByName((ViewGroup) view, "subtitle1");
                String subtitle = subtitleView.getText().toString();
                if (subtitle.endsWith("B") && !subtitle.contains("/")) {
                    String title = titleView.getText().toString();
                    String folder = getCurrentFolder();
                    Logger.v("Clicked file " + folder + "/" + title);
                    mDBHelper.fileVisited(folder, title);
                    refreshHighlight((GridView) view.getParent(), title);
                }
            }
        } catch (Throwable e) {
            Logger.e("File clicked exception.", e);
        }
    }

    private void refreshHighlight(GridView listView, String title) {
        for (int i = 0; i < listView.getChildCount(); ++i) {
            View child = listView.getChildAt(i);
            if (child.getClass().getName().equals(CHECKABLE_RELATIVE_LAYOUT)) {
                TextView titleView = (TextView) ViewUtils.findViewByName((ViewGroup) child, "title");
                if (titleView.getText().toString().equals(title)) {
                    child.setBackgroundColor(Color.DKGRAY);
                } else {
                    child.setBackground(null);
                }
            }
        }
    }

    private String getCurrentFolder() {
        int[] location = new int[2];
        mHeaderViews.get(0).getLocationOnScreen(location);
        ViewGroup headerView = (ViewGroup) (location[0] == 0  ? mHeaderViews.get(0) : mHeaderViews.get(1));
        View rootSwitchView = ViewUtils.findViewByName(headerView, "root_switch");
        TextView rootTextView = (TextView) ViewUtils.nextView(rootSwitchView);
        String path = rootTextView.getText().toString();

        if (rootTextView.getCurrentTextColor() != 0xDEFFFFFF) {
            TextView textView = (TextView) ViewUtils.nextView((View) rootSwitchView.getParent());
            while (textView != null) {
                path += "/" + textView.getText();
                if (textView.getCurrentTextColor() == 0xDEFFFFFF) {
                    break;
                }
                textView = (TextView) ViewUtils.nextView(textView);
            }
        }
        return path;
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
            execSQL(getWritableDatabase(), String.format(sql, folder.replace("'", "''"),
                    file.replace("'", "''")));
            cache.put(folder, file);
        }

        public String getVisitedFile(String folder) {
            String file = cache.get(folder);
            if (file != null) {
                return !file.isEmpty() ? file : null;
            }
            String sql = "SELECT File from Visited WHERE Folder='%1$s' LIMIT 1;";
            Cursor cursor = rawQuery(getWritableDatabase(), String.format(sql, folder.replace("'", "''")));
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
