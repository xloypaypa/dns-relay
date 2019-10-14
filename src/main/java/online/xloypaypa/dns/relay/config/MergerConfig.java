package online.xloypaypa.dns.relay.config;

import online.xloypaypa.dns.relay.network.merger.MultiRespondsMerger;

public interface MergerConfig {

    boolean isChinaOnly(int clientIndex);

    MultiRespondsMerger getMerger();
}
