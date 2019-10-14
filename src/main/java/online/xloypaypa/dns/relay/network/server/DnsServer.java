package online.xloypaypa.dns.relay.network.server;

import io.grpc.Server;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.SslContextBuilder;
import online.xloypaypa.dns.relay.config.ServerConfig;
import online.xloypaypa.dns.relay.network.client.DnsClientBuilder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class DnsServer {
    private static final Logger logger = Logger.getLogger(DnsServer.class.getName());

    private Server server;
    private final ServerConfig serverConfig;
    private final DnsClientBuilder dnsClientBuilder;

    public DnsServer(ServerConfig serverConfig, DnsClientBuilder dnsClientBuilder) {
        this.serverConfig = serverConfig;
        this.dnsClientBuilder = dnsClientBuilder;
    }

    private SslContextBuilder getSslContextBuilder() {
        ServerConfig.SSL ssl = this.serverConfig.getSsl();
        SslContextBuilder sslClientContextBuilder = SslContextBuilder.forServer(new File(ssl.getCert()),
                new File(ssl.getPrivateKey()));
        return GrpcSslContexts.configure(sslClientContextBuilder);
    }

    public void start() throws IOException {
        NettyServerBuilder nettyServerBuilder = NettyServerBuilder.forPort(this.serverConfig.getPort());
        nettyServerBuilder.executor(Executors.newFixedThreadPool(this.serverConfig.getNumberOfThread()));
        nettyServerBuilder.addService(new DnsServiceImpl(this.dnsClientBuilder));
        if (this.serverConfig.getSsl().isEnable()) {
            nettyServerBuilder.sslContext(getSslContextBuilder().build());
        }
        server = nettyServerBuilder.build().start();
        logger.info("Server started, listening on " + this.serverConfig.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            DnsServer.this.stop();
            System.err.println("*** server shut down");
        }));
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
