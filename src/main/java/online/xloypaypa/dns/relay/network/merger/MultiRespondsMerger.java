package online.xloypaypa.dns.relay.network.merger;

import online.xloypaypa.dns.relay.dns.DNSMessage;

import java.util.List;

public interface MultiRespondsMerger {
    DNSMessage mergeResponds(DNSMessage request, List<DNSMessage> responds);
}
