package online.xloypaypa.dns.relay.network.merger;

import coredns.dns.Dns;

import java.util.List;

public class DefaultMerger implements MultiRespondsMerger {
    @Override
    public Dns.DnsPacket mergeResponds(Dns.DnsPacket request, List<Dns.DnsPacket> responds) {
        for (Dns.DnsPacket now : responds) {
            if (now != null) {
                return now;
            }
        }
        return null;
    }
}
