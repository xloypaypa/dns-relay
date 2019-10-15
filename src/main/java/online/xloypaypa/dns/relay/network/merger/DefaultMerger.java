package online.xloypaypa.dns.relay.network.merger;

import online.xloypaypa.dns.relay.dns.DNSMessage;

import java.util.List;

public class DefaultMerger implements MultiRespondsMerger {
    @Override
    public DNSMessage mergeResponds(DNSMessage request, List<DNSMessage> responds) {
        for (DNSMessage now : responds) {
            if (now != null) {
                return now;
            }
        }
        return null;
    }
}
