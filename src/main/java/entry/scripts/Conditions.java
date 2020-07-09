package entry.scripts;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created By shelli On 2020/7/9 11:41
 */
@Getter
@Setter
public class Conditions {
    /**
     * 章节
     */
    private String chapter;
    /**
     * 表达式
     */
    private String expression;
    /**
     * 关键字
     */
    private List<String> keywords;

}
