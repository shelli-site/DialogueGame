package play;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import entry.GameProfile;
import entry.GameScript;
import entry.scripts.DefaultCondition;
import entry.scripts.DynamicCondition;
import entry.scripts.Stage;
import entry.scripts.choice.Action;
import entry.scripts.choice.BaseChoice;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import utils.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;


/**
 * Created By shelli On 2020/7/9 9:41
 */
@Getter
@Setter
@Slf4j
@NoArgsConstructor
public class MatchUp {
    private GameScript script;
    private String output;
    private String chapter;
    private Map<String, Object> variables;

    public MatchUp(String output, GameScript script) {
        this.output = output;
        this.script = script;
        this.variables = new HashMap<>();
    }

    public MatchUp(String chapter, String output, Map<String, Object> vars) {
        this.chapter = chapter;
        this.output = output;
        this.variables = vars;
    }

    /**
     * 显示章节剧情
     *
     * @param stage
     * @param player
     * @param vars
     */
    void displayStage(Stage stage, String player, Map<String, Object> vars) {
        String story = Optional.ofNullable(stage).map(Stage::getStory).orElse("该小节没有故事");
        displayCustom(stage, story, player, vars);
    }

    /**
     * 显示自定内容
     *
     * @param stage
     * @param defMsg
     * @param player
     * @param vars
     */
    void displayCustom(Stage stage, String defMsg, String player, Map<String, Object> vars) {
        defMsg = defMsg.replace("@sender", "@" + player).replace("@title", script.getTitle());
        for (String key : vars.keySet()) {
            defMsg = defMsg.replace("@" + key, vars.get(key).toString());
        }
        for (String key : script.getConstants().keySet()) {
            defMsg = defMsg.replace("@" + key, script.getConstants().get(key).toString());
        }
        String template = "#### [title]\n[story]\n";
        String output = template.replace("[title]", Objects.isNull(stage) ? "未知章节" : stage.getChapter()).replace("[story]", defMsg);
        this.setOutput(output);
    }

    /**
     * 判定相等
     * "1.1" == "1.1"
     * "2.2" == "2.*"
     *
     * @param template
     * @param compare
     */
    public static boolean chapterMatch(String template, String compare) {
        if (Objects.isNull(template) || "*".equals(template.trim())) {
            return true;
        } else if (template.matches(".+\\.\\*")) {
            template = template.replace(".*", "").trim();
            Float templateNum = Float.valueOf(template);
            Float compareNum = Float.valueOf(compare);
            return (templateNum - compareNum) * (templateNum - compareNum) < 1;
        } else {
            try {
                return template.trim().equals(compare.trim());
            } catch (NullPointerException e) {
                return false;
            }
        }
    }

    /**
     * 根据输入推进剧情
     *
     * @param stage
     * @param input
     * @param chapter
     * @param vars
     */
    MatchUp proceed(Stage stage, String input, String chapter, Map<String, Object> vars) {
        List<DefaultCondition> defaults = script.getDefaults();
        List<DynamicCondition> dynamics = script.getDynamics();
        List<String> variables = script.getVariables();
        input = input.toLowerCase();
        // 查找剧情选项
        int target = -1;
        loop1:
        for (int i = 0; Objects.nonNull(stage) && i < stage.getChoices().size(); i++) {
            if (stage.getChoices().get(i).getKeywords().size() == 0) {
                target = i;
                break loop1;
            }
            for (int j = 0; j < stage.getChoices().get(i).getKeywords().size(); j++) {
                if (input.indexOf(stage.getChoices().get(i).getKeywords().get(j)) != -1) {
                    target = i;
                    break loop1;
                }
            }
        }
        // 遍历缺省选项
        if (target == -1) {
            // 查找缺省回复
            loop2:
            for (int x = 0; x < defaults.size(); x++) {
                if (chapterMatch(defaults.get(x).getConditions().getChapter(), chapter)) {
                    if (defaults.get(x).getConditions().getKeywords().size() == 0) {
                        target = x;
                        break loop2;
                    }
                    for (int y = 0; y < defaults.get(x).getConditions().getKeywords().size(); y++) {
                        if (input.indexOf(defaults.get(x).getConditions().getKeywords().get(y)) != -1) {
                            target = x;
                            break loop2;
                        }
                    }
                }
            }
            if (target == -1) {
                return new MatchUp(chapter, "无匹配分支，游戏树崩塌", vars);
            }
            // 执行缺省回复
            return process(defaults.get(target), chapter, vars, variables, dynamics);
        }
        // 处理章节选项
        else {
            // 执行选择
            return process(stage.getChoices().get(target), chapter, vars, variables, dynamics);
        }

    }


    /**
     * 处理剧情选项/默认回复
     *
     * @return
     */
    private MatchUp process(BaseChoice choice, String chapter, Map<String, Object> vars, List<String> variables, List<DynamicCondition> dynamics) {
        MatchUp ret = new MatchUp(chapter, "", vars);
        // 记录回合数: rounds
        String roundsVar = "rounds";
        if (variables.indexOf(roundsVar) != -1) {
            vars.put(roundsVar, Optional.ofNullable(vars.get(roundsVar)).map(l -> Long.parseLong(l.toString()) + 1L).orElse(0L));
            ret.setVariables(vars);
        }
        // 执行选项
        execute(choice, ret, vars, variables);
        // 匹配动态条件
        // phase 0: 检查章节条件
        boolean found = false;
        for (DynamicCondition dynamic : dynamics) {
            if (chapterMatch(dynamic.getConditions().getChapter(), ret.getChapter())) {
                found = true;
            }
        }
        if (!found) {
            return ret;
        }
        // phase 1: 检查动态条件
        int targetDynamic = -1;
        for (int i = 0; i < dynamics.size(); i++) {
            DynamicCondition dynamic = dynamics.get(i);
            Boolean bool = (Boolean) evalEx(dynamic.getConditions().getExpression(), variables, ret);
            // 确保最后选中最先匹配到的条件
            if (bool && targetDynamic == -1) {
                targetDynamic = i;
            }

        }
        if (targetDynamic == -1) {
            return ret;
        }
        // 执行动态条件
        // 注意: 执行 incr、decr、calc 这两种反过来又影响了变量的条件行为时，可以改写代码，来允许再次推导动态条件。但这可能引起死循环。
        execute(dynamics.get(targetDynamic), ret, vars, variables);
        return ret;
    }

    /**
     * 动态执行代码
     *
     * @param cmd
     * @param variables
     * @param ret
     * @return
     */
    private Object evalEx(String cmd, List<String> variables, MatchUp ret) {
        ArrayList<String> cmdLines = new ArrayList<>();
        // 因为需要初始化所有变量，所以要遍历整个变量声明列表
        variables.forEach(element -> {
            String value = JSON.toJSONString(ret.getVariables().get(element));
            if ("null".equals(value)) value = "undefined";
            cmdLines.add("var " + element + " = " + value);
        });
        // 常量
        this.script.getConstants().keySet().forEach((key) -> cmdLines.add("var " + key + " = " + JSON.toJSONString(Optional.ofNullable(script.getConstants().get(key)).orElse("undefined"))));
        cmdLines.add(cmd);
        String cmdCode = CollUtil.join(cmdLines, ";\n");
        log.debug("\n[evalex begin]\n {} \n[evalex end]\n", cmdCode);
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
        Object evalRet = new Object();
        try {
            evalRet = engine.eval(cmdCode);
        } catch (ScriptException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return evalRet;
    }

    /**
     * 动态执行代码
     *
     * @param cmd
     * @param savechg
     * @return
     */
    private Object evalEx(String cmd, List<String> variables, MatchUp ret, Map<String, Object> vars, Boolean savechg) {
        Object evalRet = this.evalEx(cmd, variables, ret);
        // 原地保存修改
        if (savechg) {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
            for (String element : variables) {
                if (StringUtils.nonBlank(element)) {
                    try {
                        vars.put(element, engine.eval(element).toString());
                    } catch (ScriptException e) {
                        log.error(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
        return evalRet;
    }

    /**
     * 执行该选项的行动
     *
     * @param choice
     * @param ret
     * @param vars
     * @param variables
     */
    private boolean execute(BaseChoice choice, MatchUp ret, Map<String, Object> vars, List<String> variables) {
        boolean varChanged = false;
        // action 可以是 list(一组动作)、string(单个动作)
        // param 的类型和长度要和 action 保持一致
        ArrayList<Action> actionSet = new ArrayList<>();
        ArrayList<String> paramSet = new ArrayList<>();
        if (choice.getAction() instanceof String) {
            actionSet.add(Action.valueOf(choice.getAction().toString()));
            paramSet.add(Optional.ofNullable(choice.getParam()).map(Object::toString).orElse(null));
        } else if (choice.getAction() instanceof ArrayList) {
            for (Object a : (ArrayList) (choice.getAction())) {
                actionSet.add(Action.valueOf(a.toString()));
            }
            for (Object p : (ArrayList) (choice.getParam())) {
                paramSet.add(p.toString());
            }
        } else {
            log.error("choice action exception");
            return varChanged;
        }
        // 执行
        for (int index = 0; index < actionSet.size(); index++) {
            switch (actionSet.get(index)) {
                case jump:
                    // 章节推进
                    ret.setChapter(paramSet.get(index));
                    break;
                case gotox:
                    String chapterNext = evalEx(paramSet.get(index), variables, ret).toString();
                    ret.setChapter(chapterNext);
                    break;
                case none:
                    // 章节不变
                    if (StringUtils.nonBlank(choice.getDescription())) {
                        ret.setOutput(choice.getDescription());
                    }
                    break;
                case incr:
                    // 变量增加，章节不变
                    varChanged = true;
                    String key1 = paramSet.get(index);
                    vars.put(key1, Optional.ofNullable(vars.get(key1)).map(v -> Long.parseLong(v.toString()) + 1).orElse(1L));
                    if (StringUtils.nonBlank(choice.getDescription())) {
                        ret.setOutput(choice.getDescription());
                    }
                    ret.setVariables(vars);
                    break;
                case decr:
                    // 变量减少，章节不变
                    varChanged = true;
                    String key2 = paramSet.get(index);
                    vars.put(key2, Optional.ofNullable(vars.get(key2)).map(v -> Long.parseLong(v.toString()) - 1).orElse(1L));
                    if (StringUtils.nonBlank(choice.getDescription())) {
                        ret.setOutput(choice.getDescription());
                    }
                    ret.setVariables(vars);
                    break;
                case calc:
                    // 变量运算，章节不变
                    varChanged = true;
                    // 要对哪个变量做运算
                    String varName = "";
                    for (String element : variables) {
                        if (paramSet.get(index).contains(element)) {
                            varName = element;
                        }
                    }
                    if (StringUtils.nonBlank(varName)) {
                        vars.put(varName, evalEx(paramSet.get(index), variables, ret).toString());
                    }
                    if (StringUtils.nonBlank(choice.getDescription())) {
                        ret.setOutput(choice.getDescription());
                    }
                    ret.setVariables(vars);
                    break;
                case eval:
                    // 变量运算，章节不变
                    varChanged = true;
                    // 原地保存结果
                    evalEx(paramSet.get(index), variables, ret, vars, true);
                    if (StringUtils.nonBlank(choice.getDescription())) {
                        ret.setOutput(choice.getDescription());
                    }
                    ret.setVariables(vars);
                    break;
                case reset:
                    // 重置章节到开头，清空变量环境
                    ret.setChapter("1.1");
                    ret.setVariables(new HashMap<>());
                    break;
                default:
                    log.error("choice action exception");
                    ret.setOutput("行为配置异常，游戏树崩塌");
            }
        }
        return varChanged;
    }

    public static MatchUp play(String input, GameProfile profile, GameScript script) {
        String player = profile.getPlayer();
        String chapter = profile.getChapter();
        Map<String, Object> vars = profile.getVariables();
        log.debug("玩家: {}, 当前章节: {}, 输入: {}", player, chapter, input);
        String chapterAfter = chapter;
        MatchUp current = new MatchUp("", script);
        Stage stage = script.getStages().get(chapter);
        if (StringUtils.isBlank(input)) {
            // 播放当前剧情
            log.debug("玩家: {},当前章节: {},输入: {}", player, chapter, input);
            current.displayStage(stage, player, vars);
        } else {
            // 处理用户输入
            MatchUp result = current.proceed(stage, input, chapter, vars);
            chapterAfter = result.getChapter();
            vars = result.getVariables();
            if ((result.getChapter() == null && chapter == null) || result.getChapter().equals(chapter)) {
                // 章节没有推进
                log.debug("章节没有推进");
                if (Objects.isNull(result.getOutput())) {
                    current.displayStage(stage, player, vars);
                } else {
                    current.displayCustom(stage, result.getOutput(), player, vars);
                }
            } else {
                // 章节推进了
                log.debug("章节推进了～");
                Stage stageNext = script.getStages().get(chapterAfter);
                if (Objects.isNull(stageNext)) {
                    current.setOutput("新章节不存在");
                } else {
                    current.displayStage(stageNext, player, vars);
                }
            }
        }
        current.setChapter(chapterAfter);
        current.setVariables(vars);
        return current;
    }


    public void out() {
        System.out.println(this.output);
    }
}