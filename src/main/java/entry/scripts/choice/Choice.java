package entry.scripts.choice;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Created By shelli On 2020/7/9 12:53
 */
@Getter
@Setter
@NoArgsConstructor
public class Choice implements BaseChoice {
    /**
     * 关键词
     */
    private List<String> keywords;
    /**
     * 描述: 若选项行为没有触发章节跳转，则显示描述消息
     */
    private String description;

    /**
     * goto、none、incr、decr、calc，分别是章节跳转、无、变量自增、变量自减、变量运算
     */
    private Object action;
    private Object param;
}
