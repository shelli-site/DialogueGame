package play;

import com.esotericsoftware.yamlbeans.YamlReader;
import entry.GameProfile;
import entry.GameScript;
import lombok.extern.slf4j.Slf4j;
import utils.Save;
import utils.StringUtils;

import java.io.FileReader;
import java.util.Objects;
import java.util.Scanner;

/**
 * Created By shelli On 2020/7/8 16:55
 */
@Slf4j
public class TextGame {
    private String scriptFileName;
    private String profileFileName;
    private GameScript script;
    private GameProfile profile;
    private MatchUp scene;

    public TextGame(String scriptFileName, String profileFileName) {
        this.scriptFileName = scriptFileName;
        if (profileFileName == null) {
            this.profileFileName = this.scriptFileName.replace(".yaml", ".save");
        } else {
            this.profileFileName = profileFileName;
        }
    }

    /**
     * 方法交互（初始化）
     *
     * @param input
     * @return
     */
    public String initName(String name) {
        loadScript(scriptFileName);
        profile = Save.loadFromDisk(profileFileName);
        // 新开存档
        if (Objects.isNull(profile)) {
            String player = name;
            if (StringUtils.isBlank(player)) {
                System.out.println("那就用默认名称吧～");
                player = "玩家";
            }
            profile = new GameProfile(player);
        }
        // 继续游戏
        scene = MatchUp.play("", profile, script);
        return scene.getOutput();
    }

    /**
     * 方法交互
     *
     * @param input
     * @return
     */
    public String interactive(String input) {
        if ("exit".equals(input) || "quit".equals(input) || "退出".equals(input)) {
            return null;
        }
        // 对局
        profile.getInputs().add(input);
        Save.saveToDisk(profileFileName, profile);
        scene = MatchUp.play(input, profile, script);
        // 存档
        profile.setChapter(scene.getChapter());
        profile.setVariables(scene.getVariables());
        Save.saveToDisk(profileFileName, profile);
        // 返回剧情
        return scene.getOutput();
    }

    /**
     * 命令行式交互
     */
    public void play() {
        log.debug("开始 scriptFileName:{}, profileFileName:{}", scriptFileName, profileFileName);
        loadScript(scriptFileName);
        profile = Save.loadFromDisk(profileFileName);
        // 新开存档
        if (Objects.isNull(profile)) {
            String player = consoleIn("\n> 请输入您的大名: ");
            if (StringUtils.isBlank(player)) {
                System.out.println("那就用默认名称吧～");
                player = "玩家";
            }
            profile = new GameProfile(player);
        }
        // 继续游戏
        scene = MatchUp.play("", profile, script);
        scene.out();
        while (true) {
            String input = consoleIn("\n> 请输入您的操作: ");
            if ("exit".equals(input) || "quit".equals(input) || "退出".equals(input)) {
                break;
            }
            // 对局
            profile.getInputs().add(input);
            Save.saveToDisk(profileFileName, profile);
            scene = MatchUp.play(input, profile, script);
            // 存档
            profile.setChapter(scene.getChapter());
            profile.setVariables(scene.getVariables());
            Save.saveToDisk(profileFileName, profile);
            // 展示剧情
            scene.out();
        }
    }

    private void loadScript(String scriptFileName) {
        try {
            FileReader scriptFile = new FileReader(scriptFileName);
            YamlReader reader = new YamlReader(scriptFile);
            this.script = reader.read(GameScript.class);
        } catch (Exception e) {
            log.error("剧本加载失败");
            e.printStackTrace();
        }
    }

    private String consoleIn(String tip) {
        System.out.printf(tip);
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }
}
