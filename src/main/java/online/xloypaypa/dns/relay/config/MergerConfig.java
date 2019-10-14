package online.xloypaypa.dns.relay.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import online.xloypaypa.dns.relay.network.merger.ChinaDnsMerger;
import online.xloypaypa.dns.relay.network.merger.DefaultMerger;
import online.xloypaypa.dns.relay.network.merger.MultiRespondsMerger;

import java.util.HashSet;
import java.util.Set;

public class MergerConfig {

    private Set<Integer> chinaOnly;
    private final MultiRespondsMerger merger;

    MergerConfig(JsonObject mergerConfig) {
        String type = mergerConfig.get("type").getAsString();
        switch (type) {
            case "chinaIP":
                this.merger = new ChinaDnsMerger();
                break;
            case "default":
            default:
                this.merger = new DefaultMerger();
        }
        this.chinaOnly = new HashSet<>();
        if (mergerConfig.has("chinaOnly")) {
            JsonArray chinaOnly = mergerConfig.get("chinaOnly").getAsJsonArray();
            for (JsonElement now : chinaOnly) {
                this.chinaOnly.add(now.getAsInt());
            }
        }
    }

    public boolean isChinaOnly(int clientIndex) {
        return this.chinaOnly.contains(clientIndex);
    }

    public MultiRespondsMerger getMerger() {
        return this.merger;
    }

}
