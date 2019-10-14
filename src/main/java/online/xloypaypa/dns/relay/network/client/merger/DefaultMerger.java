package online.xloypaypa.dns.relay.network.client.merger;

import com.google.gson.JsonArray;
import coredns.dns.Dns;
import online.xloypaypa.dns.relay.network.client.MultiDnsClient;

import java.util.List;

public class DefaultMerger implements MultiDnsClient.MultiRespondsMerger {
    @Override
    public Dns.DnsPacket mergeResponds(Dns.DnsPacket request, JsonArray clients, List<Dns.DnsPacket> responds) {
        for (Dns.DnsPacket now : responds) {
            if (now != null) {
                return now;
            }
        }
        return null;
    }
}
