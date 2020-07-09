import lombok.extern.slf4j.Slf4j;
import play.TextGame;
import utils.StringUtils;

/**
 * Created By shelli On 2020/7/8 16:15
 */
@Slf4j
public class MainStart {
    private static String scriptFileName;
    private static String profileFileName;

    public static void main(String[] args) {
        readOption(args);
        TextGame textGame = new TextGame(scriptFileName, profileFileName);
        textGame.play();
    }

    private static void readOption(String[] args) {
        try {
            if (StringUtils.isBlank(args[0])) {
                throw new Exception("0");
            } else {
                scriptFileName = args[0];
            }
            if (StringUtils.nonBlank(args[1])) {
                profileFileName = args[1];
            }
        } catch (Exception e) {
            if ("0".equals(e.getMessage())) {
                log.error("必须指定一个剧本\n剧本获取: https://github.com/YicongCao/MarkdownGame/tree/master/scripts");
            }
        }
    }
}
