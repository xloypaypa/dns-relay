package online.xloypaypa.dns.relay.config;

import online.xloypaypa.dns.relay.network.merger.ChinaDnsMerger;
import online.xloypaypa.dns.relay.network.merger.DefaultMerger;
import online.xloypaypa.dns.relay.network.merger.MultiRespondsMerger;

public class MergerConfig {

    private final MultiRespondsMerger merger;

    MergerConfig(String type) {
        switch (type) {
            case "chinaIP":
                this.merger = new ChinaDnsMerger();
                break;
            case "default":
            default:
                this.merger = new DefaultMerger();
        }
    }

    public MultiRespondsMerger getMerger() {
        return this.merger;
    }

}
