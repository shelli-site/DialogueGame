package utils;

import java.util.Objects;

/**
 * Created By shelli On 2020/7/8 16:30
 */
public class StringUtils {
    public static boolean isBlank(String s) {
        try {
            if (s == null) return true;
            return "".equals(s.replaceAll("[ ]*", ""));
        } catch (Exception e) {
            return true;
        }
    }

    public static boolean nonBlank(String s) {
        return !isBlank(s);
    }

}
