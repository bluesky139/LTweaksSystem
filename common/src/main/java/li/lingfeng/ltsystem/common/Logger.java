package li.lingfeng.ltsystem.common;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;

public class Logger {

    public static String TAG = "";
    private static org.apache.log4j.Logger _logger;

    private static org.apache.log4j.Logger logger() {
        if (_logger == null) {
            _logger = org.apache.log4j.Logger.getLogger(TAG);
            _logger.setLevel(Level.TRACE);
        }
        return _logger;
    }

    static {
        BasicConfigurator.configure();
    }

    public static void v(String msg) {
        logger().trace(msg);
    }

    public static void d(String msg) {
        logger().debug(msg);
    }

    public static void i(String msg) {
        logger().info(msg);
    }

    public static void w(String msg) {
        logger().warn(msg);
    }

    public static void e(String msg) {
        logger().error(msg);
    }

    public static void e(String msg, Throwable e) {
        logger().error(msg, e);
    }
}
