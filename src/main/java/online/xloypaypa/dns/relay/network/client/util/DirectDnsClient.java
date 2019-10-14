package online.xloypaypa.dns.relay.network.client.util;

import coredns.dns.Dns;
import coredns.dns.DnsServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import online.xloypaypa.dns.relay.config.ClientConfigImpl;

import javax.net.ssl.SSLException;
import java.util.concurrent.TimeUnit;

public class DirectDnsClient {

    private final ManagedChannel channel;
    private final DnsServiceGrpc.DnsServiceBlockingStub blockingStub;

    public DirectDnsClient(ClientConfigImpl clientConfigImpl) throws SSLException {
        NettyChannelBuilder nettyChannelBuilder = NettyChannelBuilder.forAddress(clientConfigImpl.getHost(), clientConfigImpl.getPort());
        if (clientConfigImpl.getSsl().isEnable()) {
            nettyChannelBuilder.overrideAuthority(clientConfigImpl.getSsl().getServerName())
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
