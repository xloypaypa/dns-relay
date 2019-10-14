package online.xloypaypa.dns.relay.config;

import online.xloypaypa.dns.relay.network.client.MultiDnsClient;

public interface UpstreamConfig {

    MultiDnsClient getMultiDnsClient();

}
