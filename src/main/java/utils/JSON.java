package utils;

/**
 * Created By shelli On 2020/7/15 10:07
 */
public class JSON {
    public static String toJSONString(Object obj) {
        String str = com.alibaba.fastjson.JSON.toJSONString(obj);
        if (str.matches("\"\\d+\"")) {
            str = str.replace("\"", "");
        }
        return str;
    }
}
