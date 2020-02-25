package li.lingfeng.ltsystem.common;

import java.util.Iterator;

public class Utils {

    public static int indexOfReach(String str, String search, int reach) {
        int index = str.indexOf(search);
        if (index < 0) {
            return -1;
        }
        int reached = 1;
        if (reached == reach) {
            return index;
        }

        while (index >= 0) {
            index = str.indexOf(search, index + 1);
            if (index < 0) {
                return -1;
            } else {
                ++reached;
                if (reached == reach) {
                    return index;
                }
            }
        }
        return -1;
    }

    public static String removeEndWithRIndexOf(String str, char c) {
        int i = str.lastIndexOf(c);
        return i >= 0 ? str.substring(0, i) : str;
    }

    public interface JoinTProcessor<T> {
        String process(T object, int index);
    }

    public static <T> String joinT(Iterable<T> objects, String c, JoinTProcessor processor) {
        StringBuilder builder = new StringBuilder();
        Iterator<T> it = objects.iterator();
        int i = 0;
        while (it.hasNext()) {
            if (i > 0) {
                builder.append(c);
            }
            builder.append(processor.process(it.next(), i));
            ++i;
        }
        return builder.toString();
    }
}
