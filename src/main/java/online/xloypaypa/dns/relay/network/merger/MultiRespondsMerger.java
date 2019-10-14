package online.xloypaypa.dns.relay.network.merger;

import coredns.dns.Dns;

import java.util.List;

public interface MultiRespondsMerger {
    Dns.DnsPacket mergeResponds(Dns.DnsPacket request, List<Dns.DnsPacket> responds);
}
