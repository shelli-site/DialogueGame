package entry.scripts;

import entry.scripts.choice.BaseChoice;
import lombok.Getter;
import lombok.Setter;

/**
 * Created By shelli On 2020/7/9 13:27
 */
@Getter
@Setter
public class DefaultCondition implements BaseChoice {
    private Conditions conditions;
    /**
     * jump、none、incr、decr、calc，分别是章节跳转、无、变量自增、变量自减、变量运算
     */
    private Object action;

    @Override
    public Object getParam() {
        return new Object();
    }

    private String description;
}
