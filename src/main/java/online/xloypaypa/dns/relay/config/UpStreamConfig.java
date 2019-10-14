package online.xloypaypa.dns.relay.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import online.xloypaypa.dns.relay.network.client.DnsClientBuilder;
import online.xloypaypa.dns.relay.network.client.MultiDnsClient;

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
        this.dnsClientBuilder = () -> {
            try {
                return new MultiDnsClient(clients, this.executor);
            } catch (SSLException e) {
                return null;
            }
        };
    }

    public String getType() {
        return type;
    }

    public DnsClientBuilder getDnsClientBuilder() {
        return dnsClientBuilder;
    }
}
