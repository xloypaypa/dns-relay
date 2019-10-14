package online.xloypaypa.dns.relay.network.client;

import coredns.dns.Dns;

public interface DnsClient {

    Dns.DnsPacket query(Dns.DnsPacket request) throws Exception;

}
