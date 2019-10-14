package online.xloypaypa.dns.relay.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import online.xloypaypa.dns.relay.network.client.MultiDnsClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpStreamConfig {

    private final MultiDnsClient multiDnsClient;

    UpStreamConfig(JsonObject upstreamConfig) {
        JsonArray clients = upstreamConfig.get("upstreams").getAsJsonArray();
        int threads = clients.size() * 2;
        if (upstreamConfig.has("threads")) {
            threads = upstreamConfig.get("threads").getAsInt();
        }
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        this.multiDnsClient = new MultiDnsClient(clients, executor);
    }

    public MultiDnsClient getMultiDnsClient() {
        return multiDnsClient;
    }
}
