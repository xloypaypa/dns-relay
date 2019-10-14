package online.xloypaypa.dns.relay.network.merger;

import com.google.gson.JsonArray;
import coredns.dns.Dns;

import java.util.List;

public interface MultiRespondsMerger {
    Dns.DnsPacket mergeResponds(Dns.DnsPacket request, JsonArray clients, List<Dns.DnsPacket> responds);
}
