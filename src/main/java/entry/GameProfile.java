package entry;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created By shelli On 2020/7/9 9:48
 */
@Getter
@Setter
public class GameProfile {
    private String player;
    private List<String> inputs;
    private String chapter;
    private Map<String, Long> variables;

    public GameProfile(String player) {
        this.player = player;
        this.chapter = "1.1";
        this.inputs = new ArrayList<>();
        this.variables = new HashMap<>();
    }
}
