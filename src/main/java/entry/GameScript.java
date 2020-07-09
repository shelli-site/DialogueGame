package entry;

import entry.scripts.DefaultCondition;
import entry.scripts.DynamicCondition;
import entry.scripts.Stage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Created By shelli On 2020/7/8 17:23
 */
@Getter
@Setter
public class GameScript {
    private String title;
    private String msgtype;
    /**
     * 变量: 可以记录某些变量的值、动态触发某些章节
     */
    private List<String> variables;
    /**
     * 常量
     */
    private Map<String, Object> constants;
    /**
     * 舞台: 由不定数量的章节组成游戏本体
     */
    private Map<String, Stage> stages;
    /**
     * 动态条件: 输入选项结算后，会检查动态条件，若条件成立，则执行操作
     */
    private List<DynamicCondition> dynamics;
    /**
     * 默认区: 当用户输入没有命中章节内所覆盖的选项时，走到这里
     */
    private List<DefaultCondition> defaults;
}
