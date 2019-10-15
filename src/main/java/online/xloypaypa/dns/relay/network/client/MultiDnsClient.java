package online.xloypaypa.dns.relay.network.client;

import coredns.dns.Dns;
import online.xloypaypa.dns.relay.config.UpstreamConfig;
import online.xloypaypa.dns.relay.network.client.util.DirectDnsClient;

import javax.net.ssl.SSLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class MultiDnsClient {

    private final UpstreamConfig.ClientConfig[] clientConfigs;
    private final ExecutorService executor;

    public MultiDnsClient(List<UpstreamConfig.ClientConfig> clientConfigs, ExecutorService executor) {
        this.clientConfigs = clientConfigs.toArray(UpstreamConfig.ClientConfig[]::new);
        this.executor = executor;
    }

    public List<Dns.DnsPacket> query(Dns.DnsPacket request) throws InterruptedException, SSLException {
        DirectDnsClient[] directDnsClients = generateDirectDnsClients();

        List<Future<Dns.DnsPacket>> futures = Arrays.stream(directDnsClients)
                .map(directDnsClient -> executor.submit(() -> directDnsClient.query(request)))
                .collect(Collectors.toList());

        while (futures.stream().allMatch(Future::isDone)) {
            Thread.sleep(50);
        }
        return futures.stream().map(now -> {
            try {
                return now.get();
            } catch (InterruptedException | ExecutionException e) {
                return null;
            }
        }).collect(Collectors.toList());
    }

    private DirectDnsClient[] generateDirectDnsClients() throws SSLException {
        DirectDnsClient[] directDnsClients = new DirectDnsClient[this.clientConfigs.length];
        for (int i = 0; i < this.clientConfigs.length; i++) {
            directDnsClients[i] = new DirectDnsClient(clientConfigs[i]);
        }
        return directDnsClients;
    }

}
