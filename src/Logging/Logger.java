package Logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Logger {
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static void startExec(String id, Date startTime) {
        System.out.printf(id + "::" + getTime() + "::Started with %d ms delay. \n", System.currentTimeMillis() - startTime.getTime());
    }

    public static void endExec(String id, long expected, long actual) {
        System.out.printf(id + "::" + getTime() + "::Ended, actual %d seconds, expected %d seconds" + "%n", actual, expected);
    }

    private static String getTime() {
        return dateFormat.format(Calendar.getInstance().getTime());
    }
}
