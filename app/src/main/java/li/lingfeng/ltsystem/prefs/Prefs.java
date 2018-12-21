package li.lingfeng.ltsystem.prefs;

public class Prefs {

    private static PreferenceStore _instance = new PreferenceStore("persist.sys.ltweaks.");
    public static PreferenceStore instance() {
        return _instance;
    }
}
