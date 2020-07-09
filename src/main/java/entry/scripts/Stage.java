package entry.scripts;

import entry.scripts.choice.Choice;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created By shelli On 2020/7/9 11:38
 */
@Getter
@Setter
public class Stage {
    /**
     * 章节名称，会显示在消息标题的位置
     */
    private String chapter;
    /**
     * 本节剧情
     */
    private String story;
    /**
     * 输入选项: 关键词匹配，用户输入若含有该词，视为选择了此选项
     */
    private List<Choice> choices;
}
