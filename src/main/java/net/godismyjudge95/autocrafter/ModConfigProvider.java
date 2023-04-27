package net.godismyjudge95.autocrafter;

import com.mojang.datafixers.util.Pair;
import net.godismyjudge95.autocrafter.helpers.SimpleConfig;

import java.util.ArrayList;
import java.util.List;

public class ModConfigProvider implements SimpleConfig.DefaultConfig {

    private String configContents = "";

    public List<Pair<String, ?>> getConfigsList() {
        return configsList;
    }

    private final List<Pair<String, ?>> configsList = new ArrayList<>();

    public void addKeyValuePair(Pair<String, ?> keyValuePair, String comment) {
        configsList.add(keyValuePair);
        configContents += keyValuePair.getFirst() + "=" + keyValuePair.getSecond() + " #"
                + comment + " | default: " + keyValuePair.getSecond() + "\n";
    }

    public void addComment(String comment) {
        configContents += comment + "\n\n";
    }

    @Override
    public String get(String namespace) {
        return configContents;
    }
}
