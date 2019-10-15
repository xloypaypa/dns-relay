package online.xloypaypa.dns.relay;

import online.xloypaypa.dns.relay.config.Config;
import online.xloypaypa.dns.relay.network.client.MultiDnsClient;
import online.xloypaypa.dns.relay.network.server.DnsServer;

import java.io.IOException;

public class Main {

    private final Config config;

    private Main(Config config) {
        this.config = config;
    }

    private void startServer() throws IOException, InterruptedException {
        DnsServer server = new DnsServer(this.config.getServerConfig(), new MultiDnsClient(config.getUpstreamConfig()));
        server.start();
        server.blockUntilShutdown();

    }

    public static void main(String[] args) throws Exception {
        Main main = new Main(Config.getConfig());
        main.startServer();
    }

}
