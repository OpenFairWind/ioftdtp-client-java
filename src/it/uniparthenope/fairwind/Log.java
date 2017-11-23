package it.uniparthenope.fairwind;

/**
 * Created by raffaelemontella on 07/07/2017.
 */
public class Log {

    public static void i(String tag, String message) {
        System.out.println(tag+":INFO :"+message);
    }

    public static void d(String tag, String message) {
        System.out.println(tag+":DEBUG:"+message);
    }

    public static void e(String tag, String message) {
        System.out.println(tag+":ERROR:"+message);
    }
}
