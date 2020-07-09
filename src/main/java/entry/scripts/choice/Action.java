package entry.scripts.choice;

import lombok.Getter;

/**
 * Created By shelli On 2020/7/9 12:21
 */
@Getter
public enum Action {
    /**
     * 行为: jump、none、incr、decr、calc，分别是章节跳转、无、变量自增、变量自减、变量运算
     */
    jump, gotox, none, incr, decr, calc, eval, reset

}
