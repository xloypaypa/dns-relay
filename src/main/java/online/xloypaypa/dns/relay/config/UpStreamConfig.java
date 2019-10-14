package online.xloypaypa.dns.relay.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import online.xloypaypa.dns.relay.network.client.MultiDnsClient;
import online.xloypaypa.dns.relay.network.client.DirectDnsClient;
import online.xloypaypa.dns.relay.network.client.DnsClientBuilder;
import online.xloypaypa.dns.relay.network.merger.ChinaDnsMerger;
import online.xloypaypa.dns.relay.network.merger.DefaultMerger;
import online.xloypaypa.dns.relay.network.merger.MultiRespondsMerger;

import javax.net.ssl.SSLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpStreamConfig {

    private final String type;
    private final DnsClientBuilder dnsClientBuilder;
    private final ExecutorService executor;

    UpStreamConfig(JsonObject upstreamConfig) {
        this.type = upstreamConfig.get("type").getAsString();
        JsonArray clients = upstreamConfig.get("clients").getAsJsonArray();
        this.executor = Executors.newFixedThreadPool(clients.size() * 2);
        if ("multi".equals(this.type)) {
            this.dnsClientBuilder = () -> {
                try {
                    return new MultiDnsClient(clients, this.executor, merger(upstreamConfig.get("merger").getAsString()));
                } catch (SSLException e) {
                    return null;
                }
            };
        } else {
            JsonObject firstClient = clients.get(0).getAsJsonObject();
            this.dnsClientBuilder = () -> {
                try {
                    return new DirectDnsClient(new ClientConfig(firstClient));
                } catch (SSLException e) {
                    return null;
                }
            };
        }
    }

    private MultiRespondsMerger merger(String type) {
        switch (type) {
            case "default":
                return new DefaultMerger();
            case "chinaIP":
                return new ChinaDnsMerger();
            default:
                return null;
        }
    }

    public String getType() {
        return type;
    }

    public DnsClientBuilder getDnsClientBuilder() {
        return dnsClientBuilder;
    }
}
