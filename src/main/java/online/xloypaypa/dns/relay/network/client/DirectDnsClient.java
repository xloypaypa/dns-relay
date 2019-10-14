package online.xloypaypa.dns.relay.network.client;

import coredns.dns.Dns;
import coredns.dns.DnsServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import online.xloypaypa.dns.relay.config.ClientConfig;

import javax.net.ssl.SSLException;
import java.util.concurrent.TimeUnit;

public class DirectDnsClient implements DnsClient {

    private final ManagedChannel channel;
    private final DnsServiceGrpc.DnsServiceBlockingStub blockingStub;

    DirectDnsClient(ClientConfig clientConfig) throws SSLException {
        NettyChannelBuilder nettyChannelBuilder = NettyChannelBuilder.forAddress(clientConfig.getHost(), clientConfig.getPort());
        if (clientConfig.getSsl().isEnable()) {
            nettyChannelBuilder.overrideAuthority(clientConfig.getSsl().getServerName())
                    .sslContext(GrpcSslContexts.forClient().build());
        } else {
            nettyChannelBuilder.usePlaintext();
        }
        this.channel = nettyChannelBuilder.build();
        blockingStub = DnsServiceGrpc.newBlockingStub(channel);
    }

    private void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    @Override
    public Dns.DnsPacket query(Dns.DnsPacket request) throws InterruptedException {
        Dns.DnsPacket result;
        try {
            result = blockingStub.query(request);
        } finally {
            this.shutdown();
        }
        return result;
    }
}
