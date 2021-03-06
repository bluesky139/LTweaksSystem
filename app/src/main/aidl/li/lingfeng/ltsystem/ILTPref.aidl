package li.lingfeng.ltsystem;

import li.lingfeng.ltsystem.ILTPrefListener;

interface ILTPref {
    List<String> getStringList(String key);
    void putStringList(String key, in List<String> value);
    void appendStringToList(String key, String value, int limit);
    void addListener(String key, in ILTPrefListener listener);
    void removeListener(String key, in ILTPrefListener listener);
}