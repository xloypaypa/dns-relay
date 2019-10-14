package online.xloypaypa.dns.relay.network.client;

import com.google.gson.JsonArray;
import coredns.dns.Dns;
import online.xloypaypa.dns.relay.config.ClientConfig;

import javax.net.ssl.SSLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class MultiDnsClient implements DnsClient {

    private final JsonArray clients;
    private final DirectDnsClient[] directDnsClients;
    private final ExecutorService executor;
    private final MultiRespondsMerger multiRespondsMerger;

    public MultiDnsClient(JsonArray clients, ExecutorService executor, MultiRespondsMerger multiRespondsMerger) throws SSLException {
        this.clients = clients;
        DirectDnsClient[] directDnsClients = new DirectDnsClient[clients.size()];
        for (int i = 0; i < clients.size(); i++) {
            directDnsClients[i] = new DirectDnsClient(new ClientConfig(clients.get(i).getAsJsonObject()));
        }
        this.directDnsClients = directDnsClients;
        this.executor = executor;
        this.multiRespondsMerger = multiRespondsMerger;
    }

    @Override
    public Dns.DnsPacket query(Dns.DnsPacket request) throws InterruptedException {
        List<Future<Dns.DnsPacket>> futures = Arrays.stream(directDnsClients)
                .map(directDnsClient -> executor.submit(() -> directDnsClient.query(request)))
                .collect(Collectors.toList());

        while (futures.stream().allMatch(Future::isDone)) {
            Thread.sleep(50);
        }
        return this.multiRespondsMerger.mergeResponds(request, clients, futures.stream().map(now -> {
            try {
                return now.get();
            } catch (InterruptedException | ExecutionException e) {
                return null;
            }
        }).collect(Collectors.toList()));
    }

    public interface MultiRespondsMerger {
        Dns.DnsPacket mergeResponds(Dns.DnsPacket request, JsonArray clients, List<Dns.DnsPacket> responds);
    }
}
