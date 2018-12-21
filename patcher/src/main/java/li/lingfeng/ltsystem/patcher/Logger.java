package li.lingfeng.ltsystem.patcher;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;

public class Logger {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("Patcher");

    static {
        BasicConfigurator.configure();
        logger.setLevel(Level.TRACE);
    }

    public static void v(String msg) {
        logger.trace(msg);
    }

    public static void d(String msg) {
        logger.debug(msg);
    }

    public static void i(String msg) {
        logger.info(msg);
    }

    public static void w(String msg) {
        logger.warn(msg);
    }

    public static void e(String msg) {
        logger.error(msg);
    }

    public static void e(String msg, Throwable e) {
        logger.error(msg, e);
    }
}
